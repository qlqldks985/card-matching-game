package com.jjh.cardgame.ranking.dto;

import com.jjh.cardgame.ranking.domain.GameRecord;

import java.time.Instant;

/**
 * 랭킹 한 줄, 또는 방금 등록한 기록.
 *
 * @param rank     순위. 봇 의심으로 랭킹에서 빠진 기록이면 null
 * @param excluded 봇 의심으로 랭킹에서 빠졌는지 (이유는 알려 주지 않는다)
 */
public record RankingEntry(long id, Integer rank, String nickname, int pairs, int moves, long elapsedMillis,
                           Instant playedAt, boolean excluded) {

    public static RankingEntry of(GameRecord record, Integer rank) {
        return new RankingEntry(record.getId(), rank, record.getNickname(), record.getPairs(),
                record.getMoves(), record.getElapsedMillis(), record.getCreatedAt(), record.isFlagged());
    }
}
