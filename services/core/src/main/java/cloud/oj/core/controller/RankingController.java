package cloud.oj.core.controller;

import cloud.oj.core.entity.PagedList;
import cloud.oj.core.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /**
     * 获取排行榜
     */
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getRankingList(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "15") Integer limit) {
        var rankings = PagedList.resolve(rankingService.getRanking(page, limit));
        return rankings.getCount() > 0 ?
                ResponseEntity.ok(rankings)
                : ResponseEntity.noContent().build();
    }

    /**
     * 获取竞赛排行榜
     */
    @GetMapping(path = "contest/{contestId}")
    public ResponseEntity<?> getContestRanking(@PathVariable Integer contestId,
                                               @RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "15") Integer limit) {
        var rankings = PagedList.resolve(rankingService.getContestRanking(contestId, page, limit));
        return rankings.getCount() > 0 ?
                ResponseEntity.ok(rankings)
                : ResponseEntity.noContent().build();
    }

    /**
     * 获取竞赛排行榜(管理员用)
     */
    @GetMapping(path = "admin/contest/{contestId}")
    public ResponseEntity<?> getRankingListAdmin(@PathVariable Integer contestId,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "15") Integer limit) {
        var rankings = PagedList.resolve(rankingService.getContestRanking(contestId, page, limit));
        return rankings.getCount() > 0 ?
                ResponseEntity.ok(rankings)
                : ResponseEntity.noContent().build();
    }

    /**
     * 获取用户的详细得分情况
     */
    @GetMapping(path = "admin/contest/detail")
    public ResponseEntity<?> getDetail(Integer contestId, Integer uid) {
        var detail = rankingService.getDetail(contestId, uid);
        return !detail.isEmpty() ?
                ResponseEntity.ok(detail)
                : ResponseEntity.noContent().build();
    }
}
