package cloud.oj.core.dao;

import cloud.oj.core.entity.Solution;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SolutionDao {
    Solution getSolutionByUidAndTime(Integer uid, Long time, boolean showPassed);

    List<List<?>> getSolutionsByUser(Integer uid, Integer start, Integer limit, Integer filter, String filterValue);

    void updateTitle(String title, int problemId);

    void deleteByUser(Integer userId);
}
