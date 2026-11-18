package com.jjh.cardgame.ranking.service;

import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.game.service.GameService;
import com.jjh.cardgame.ranking.domain.GameRecord;
import com.jjh.cardgame.ranking.domain.GameRecordRepository;
import com.jjh.cardgame.ranking.dto.RankingEntry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Service
public class RankingService {

    public static final int MAX_LIMIT = 50;

    private final GameRecordRepository repository;
    private final GameService gameService;
    private final Clock clock;

    public RankingService(GameRecordRepository repository, GameService gameService, Clock clock) {
        this.repository = repository;
        this.gameService = gameService;
        this.clock = clock;
    }

    /** 끝난 게임의 결과를 닉네임과 함께 저장하고 순위를 돌려준다 */
    @Transactional
    public RankingEntry submit(String gameId, String nickname) {
        if (repository.existsByGameId(gameId)) {
            throw new DuplicateRecordException();
        }
        Game game = gameService.find(gameId);
        if (!game.isFinished()) {
            throw new IllegalStateException("아직 끝나지 않은 게임입니다.");
        }

        GameRecord record = new GameRecord(game.getId(), nickname.strip(), game.totalPairs(),
                game.getMoves(), game.elapsed().toMillis(), clock.instant());
        try {
            repository.saveAndFlush(record);
        } catch (DataIntegrityViolationException e) {
            // 같은 게임을 동시에 두 번 등록한 경우 (유니크 제약에 걸림)
            throw new DuplicateRecordException();
        }
        gameService.remove(gameId); // 기록이 남았으니 메모리에서는 바로 지운다

        long ahead = repository.countAhead(record.getPairs(), record.getMoves(),
                record.getElapsedMillis(), record.getId());
        return RankingEntry.of(record, (int) ahead + 1);
    }

    @Transactional(readOnly = true)
    public List<RankingEntry> top(int pairs, int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit 은 1~" + MAX_LIMIT + " 사이여야 합니다.");
        }
        List<GameRecord> records = repository.findByPairsOrderByMovesAscElapsedMillisAscIdAsc(
                pairs, PageRequest.of(0, limit));
        List<RankingEntry> result = new ArrayList<>(records.size());
        for (int i = 0; i < records.size(); i++) {
            result.add(RankingEntry.of(records.get(i), i + 1));
        }
        return result;
    }
}
