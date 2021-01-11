package group._204.oj.judge.component;

import group._204.oj.judge.error.UnsupportedLanguageError;
import group._204.oj.judge.model.Compile;
import group._204.oj.judge.model.Solution;
import group._204.oj.judge.type.Language;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 编译模块
 */
@Slf4j
@Component
class Compiler {

    @Value("${project.code-dir}")
    private String codeDir;

    @Value("${project.runner-image}")
    private String runnerImage;

    private final ProcessBuilder processBuilder = new ProcessBuilder();

    private static class CompileError extends Exception {
        CompileError(String msg) {
            super(msg);
        }
    }

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        File dir = new File(codeDir);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("目录 {} 不存在, 已创建.", codeDir);
            } else {
                log.info("无法创建目录 {}.", codeDir);
            }
        }
    }

    /**
     * 编译入口
     *
     * @param solution {@link Solution}
     * @return {@link Compile} 编译结果
     */
    public Compile compile(Solution solution) {
        String solutionId = solution.getSolutionId();
        Integer languageId = solution.getLanguage();
        String sourceCode = solution.getSourceCode();

        String src = writeCode(solutionId, languageId, sourceCode);

        if (src.isEmpty()) {
            String err = "编译错误: 无法写入源码";
            log.error(err);
            return new Compile(solutionId, -1, err);
        }

        try {
            Language language = Language.get(languageId);
            return compileSource(solutionId, language);
        } catch (UnsupportedLanguageError e) {
            log.error("编译错误: {}", e.getMessage());
            return new Compile(solutionId, -1, e.getMessage());
        }
    }

    /**
     * 根据语言类型编译源码
     *
     * @param solutionId 答案 Id
     * @param language   {@link Language}
     * @return {@link Compile} 编译结果
     */
    public Compile compileSource(String solutionId, Language language) throws UnsupportedLanguageError {

        if (language == null) {
            throw new UnsupportedLanguageError("不支持的语言: null.");
        }

        String solutionDir = codeDir + solutionId;
        List<String> cmd = new ArrayList<>(Arrays.asList("docker", "run", "--rm", "--network", "none",
                "-v", solutionDir + ":/tmp/code", "-w", "/tmp/code", runnerImage));

        // 构造编译命令
        switch (language) {
            case C:
                cmd.addAll(Arrays.asList("gcc", "-std=c11", "Solution.c", "-o", "Solution"));
                break;
            case CPP:
                cmd.addAll(Arrays.asList("g++", "-std=c++17", "Solution.cpp", "-o", "Solution"));
                break;
            case JAVA:
                cmd.addAll(Arrays.asList("javac", "-encoding", "UTF-8", "Solution.java"));
                break;
            case C_SHARP:
                cmd.addAll(Arrays.asList("mcs", "Solution.cs"));
                break;
            case KOTLIN:
                cmd.addAll(Arrays.asList("kotlinc", "Solution.kt"));
                break;
            case PYTHON:
            case BASH:
            case JAVA_SCRIPT:
                return new Compile(solutionId, 0);
            default:
                throw new UnsupportedLanguageError(String.format("不支持的语言: %s.", language));
        }

        try {
            processBuilder.command(cmd);
            Process process = processBuilder.start();
            process.waitFor();
            // 获取错误流，为空说明编译成功
            String error = getOutput(process.getErrorStream());

            if (error.isEmpty()) {
                log.debug("编译成功: solutionId={}", solutionId);
                return new Compile(solutionId, 0, null);
            } else {
                throw new CompileError(error);
            }
        } catch (IOException | InterruptedException | CompileError e) {
            log.error("编译错误: solutionId={}", e.getMessage());
            return new Compile(solutionId, -1, e.getMessage());
        }
    }

    /**
     * 从 Process 的输入流或错误流获取输出
     *
     * @param inputStream Process 的输入流（stdout or stderr）
     * @return Process 的输出
     */
    @SneakyThrows
    private String getOutput(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String out = reader.lines().collect(Collectors.joining("\n"));
        reader.close();
        return out;
    }

    /**
     * 将源码写入文件
     *
     * @param solutionId 答案 Id
     * @param language   语言 Id
     * @param source     源码
     * @return 文件路径
     */
    private String writeCode(String solutionId, int language, String source) {
        File file;
        File solutionDir = new File(codeDir + solutionId);

        if (!solutionDir.mkdirs()) {
            log.error("无法创建目录 {}", solutionDir.getName());
            return "";
        }

        if (Language.get(language) == Language.JAVA) {
            file = new File(solutionDir + "/Solution.java");
        } else if (Language.get(language) == Language.C_SHARP) {
            file = new File(solutionDir + "/Solution.cs");
        } else {
            file = new File(solutionDir + "/Solution" + Language.getExt(language));
        }

        try {
            if (file.exists() || file.createNewFile()) {
                FileWriter writer = new FileWriter(file, false);
                writer.write(source);
                writer.flush();
                writer.close();

                return file.getPath();
            } else {
                log.error("无法创建文件 {}", file.getName());
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return "";
    }
}
