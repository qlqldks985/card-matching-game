package com.jjh.cardgame.ranking.web;

import com.jjh.cardgame.ranking.dto.RankingEntry;
import com.jjh.cardgame.ranking.dto.SubmitRecordRequest;
import com.jjh.cardgame.ranking.service.RankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /** 끝난 게임의 기록 등록 */
    @PostMapping("/api/games/{gameId}/record")
    @ResponseStatus(HttpStatus.CREATED)
    public RankingEntry submit(@PathVariable String gameId, @Valid @RequestBody SubmitRecordRequest request) {
        return rankingService.submit(gameId, request.nickname());
    }

    /** 난이도별 랭킹 */
    @GetMapping("/api/rankings")
    public List<RankingEntry> rankings(@RequestParam(defaultValue = "8") int pairs,
                                       @RequestParam(defaultValue = "10") int limit) {
        return rankingService.top(pairs, limit);
    }
}
