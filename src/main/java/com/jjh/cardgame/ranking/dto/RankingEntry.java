package com.jjh.cardgame.ranking.dto;

import com.jjh.cardgame.ranking.domain.GameRecord;

import java.time.Instant;

public record RankingEntry(long id, int rank, String nickname, int pairs, int moves, long elapsedMillis, Instant playedAt) {

    public static RankingEntry of(GameRecord record, int rank) {
        return new RankingEntry(record.getId(), rank, record.getNickname(), record.getPairs(),
                record.getMoves(), record.getElapsedMillis(), record.getCreatedAt());
    }
}
