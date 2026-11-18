package com.jjh.cardgame.ranking.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    boolean existsByGameId(String gameId);

    /** 순위: 시도 횟수가 적을수록, 같으면 시간이 짧을수록, 그것도 같으면 먼저 등록한 기록이 위 */
    List<GameRecord> findByPairsOrderByMovesAscElapsedMillisAscIdAsc(int pairs, Pageable pageable);

    /** 같은 난이도에서 이 기록보다 앞선 기록 수 (+1 하면 순위) */
    @Query("""
            select count(r) from GameRecord r
            where r.pairs = :pairs
              and (r.moves < :moves
                or (r.moves = :moves and r.elapsedMillis < :elapsedMillis)
                or (r.moves = :moves and r.elapsedMillis = :elapsedMillis and r.id < :id))
            """)
    long countAhead(@Param("pairs") int pairs, @Param("moves") int moves,
                    @Param("elapsedMillis") long elapsedMillis, @Param("id") long id);
}
