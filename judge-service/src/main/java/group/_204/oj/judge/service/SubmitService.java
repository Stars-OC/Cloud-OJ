package group._204.oj.judge.service;

import group._204.oj.judge.dao.SolutionDao;
import group._204.oj.judge.dao.SourceCodeDao;
import group._204.oj.judge.model.CommitData;
import group._204.oj.judge.model.Solution;
import group._204.oj.judge.model.SourceCode;
import group._204.oj.judge.type.SolutionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class SubmitService {

    @Resource
    private SolutionDao solutionDao;

    @Resource
    private SourceCodeDao sourceCodeDao;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private Queue judgeQueue;

    /**
     * 保存提交到数据库
     * <p>隔离级别：读未提交</p>
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = Exception.class)
    public void submit(CommitData commitData) {
        var solution = new Solution(
                commitData.getSolutionId(),
                commitData.getUserId(),
                commitData.getProblemId(),
                commitData.getContestId(),
                commitData.getLanguage(),
                commitData.getType(),
                commitData.getSubmitTime()
        );

        var sourceCode = new SourceCode(solution.getSolutionId(), commitData.getSourceCode());

        solutionDao.add(solution);
        sourceCodeDao.add(sourceCode);

        solution.setSourceCode(commitData.getSourceCode());
        solution.setState(SolutionState.IN_JUDGE_QUEUE);
        solutionDao.update(solution);

        rabbitTemplate.convertAndSend(judgeQueue.getName(), solution);
    }
}
