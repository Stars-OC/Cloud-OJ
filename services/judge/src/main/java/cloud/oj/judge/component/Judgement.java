package cloud.oj.judge.component;

import cloud.oj.judge.config.AppConfig;
import cloud.oj.judge.constant.Language;
import cloud.oj.judge.constant.State;
import cloud.oj.judge.dao.ProblemDao;
import cloud.oj.judge.dao.RankingDao;
import cloud.oj.judge.dao.SolutionDao;
import cloud.oj.judge.entity.JudgeResult;
import cloud.oj.judge.entity.Problem;
import cloud.oj.judge.entity.Solution;
import cloud.oj.judge.error.UnsupportedLanguageError;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.StringJoiner;

import static cloud.oj.judge.constant.Language.*;
import static cloud.oj.judge.constant.Result.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Judgement {

    private final AppConfig appConfig;

    private final ObjectMapper objectMapper;

    private final ProblemDao problemDao;

    private final SolutionDao solutionDao;

    private final RankingDao rankingDao;

    private final Compiler compiler;

    private final HashMap<String, Integer> cpuMap;

    private final static UnixDomainSocketAddress addr = UnixDomainSocketAddress.of("/var/run/judge.sock");

    /**
     * 判题入口
     * <p>隔离级别：读提交</p>
     *
     * @param solution {@link Solution}
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void judge(Solution solution) {
        var compile = compiler.compile(solution);

        if (compile.getState() == 0) {
            // 编译成功
            var problem = problemDao.getById(solution.getProblemId());
            // 更新为正在运行状态
            solutionDao.updateState(solution.getSolutionId(), State.RUNNING);
            // 运行
            var result = execute(solution, problem);
            saveResult(solution, result, problem);
        } else {
            // 编译失败
            solution.endWithError(CE, compile.getInfo());
            solutionDao.updateWithResult(solution);
        }
    }

    /**
     * 保存判题结果
     * <p>计算分数并更新排名</p>
     *
     * @param result {@link JudgeResult}
     */
    private void saveResult(Solution solution, JudgeResult result, Problem problem) {
        // 运行错误/内部错误
        if (result.getResult().equals(RE) || result.getResult().equals(IE)) {
            log.warn("运行时/内部错误({}): {}", solution.getSolutionId(), result.getError());
            solution.endWithError(result.getResult(), result.getError());
            solutionDao.updateWithResult(solution);
            return;
        }

        var uid = solution.getUid();
        var problemId = solution.getProblemId();
        var contestId = solution.getContestId();

        var passRate = result.getPassRate();

        if (Double.isNaN(passRate)) {
            passRate = 0d;
        }

        // 查询历史提交中的最高分
        var maxScore = solutionDao.getMaxScoreOfUser(uid, problemId, contestId);

        solution.setTotal(result.getTotal());
        solution.setPassed(result.getPassed());
        solution.setPassRate(passRate);
        solution.setScore(passRate * problem.getScore());
        solution.setTime(result.getTime());
        solution.setMemory(result.getMemory());
        solution.setResult(result.getResult());
        solution.setState(State.JUDGED);

        solutionDao.updateWithResult(solution);

        // 更新排名
        // 本次得分不为 0 且历史最高分小于本次得分时才更新排名
        if (passRate > 0 && (maxScore == null || maxScore < solution.getScore())) {
            if (contestId == null) {
                rankingDao.update(uid, solution.getSubmitTime());
            } else {
                rankingDao.updateForContest(contestId, uid, solution.getSubmitTime());
            }
        } else {
            // 仅更新提交次数
            if (contestId == null) {
                rankingDao.incCommitted(uid, solution.getSubmitTime());
            } else {
                rankingDao.incCommittedForContest(uid, contestId, solution.getSubmitTime());
            }
        }
    }

    /**
     * 运行用户程序
     *
     * @return 运行结果 {@link JudgeResult}
     */
    private JudgeResult execute(Solution solution, Problem problem) {
        JudgeResult result;

        try {
            var testDataDir = appConfig.getFileDir() + "data/" + solution.getProblemId();
            var argv = buildCommand(solution, problem, testDataDir);
            var buf = ByteBuffer.allocate(2048);
            var channel = SocketChannel.open(StandardProtocolFamily.UNIX);
            channel.connect(addr);
            channel.write(ByteBuffer.wrap(argv.getBytes()));
            channel.read(buf);
            channel.close();
            buf.flip();
            var bytes = new byte[buf.remaining()];
            buf.get(bytes);
            result = objectMapper.readValue(bytes, JudgeResult.class);

            if (result.getCode() == 1) {
                result.setResult(RE);
            } else if (result.getCode() == 2) {
                result.setResult(IE);
            }
        } catch (IOException | UnsupportedLanguageError e) {
            result = new JudgeResult();
            result.setResult(IE);
            result.setError(e.getMessage());
        }

        return result;
    }

    /**
     * 生成命令
     */
    private String buildCommand(Solution solution, Problem problem, String dataDir)
            throws UnsupportedLanguageError {
        var language = solution.getLanguage();
        Language.check(language);

        var cpu = cpuMap.get(Thread.currentThread().getName());
        var workDir = appConfig.getCodeDir() + solution.getSolutionId();
        var timeLimit = problem.getTimeout();
        var memoryLimit = problem.getMemoryLimit();
        var outputLimit = problem.getOutputLimit();

        StringJoiner argv = new StringJoiner(" ");

        switch (language) {
            case C, CPP, GO -> argv.add("--cmd=./Solution");
            case JAVA -> argv.add("--cmd=java@@Solution");
            case KOTLIN -> argv.add("--cmd=./Solution.kexe");
            case JAVA_SCRIPT -> argv.add("--cmd=node@Solution.js");
            case PYTHON -> argv.add("--cmd=python3@Solution.py");
            case BASH -> argv.add("--cmd=sh@Solution.sh");
            case C_SHARP -> argv.add("--cmd=mono@Solution.exe");
        }

        return argv.add("--time=" + timeLimit)
                .add("--ram=" + memoryLimit)
                .add("--output=" + outputLimit)
                .add("--workdir=" + workDir)
                .add("--data=" + dataDir)
                .add("--lang=" + solution.getLanguage())
                .add("--cpu=" + cpu)
                .toString();
    }
}
