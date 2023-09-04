package cloud.oj.judge.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubmitData {
    private String solutionId;
    private Integer uid;
    private Integer problemId;
    private Integer contestId;
    private String sourceCode;
    private Integer language;
    private Long submitTime;
}
