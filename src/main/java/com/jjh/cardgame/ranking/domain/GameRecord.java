package com.jjh.cardgame.ranking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * 끝난 게임 한 판의 기록. 시도 횟수와 시간은 클라이언트가 보낸 값이 아니라
 * 서버가 직접 판정한 Game 에서 가져오므로 조작할 수 없다.
 */
@Entity
@Table(name = "game_record",
        uniqueConstraints = @UniqueConstraint(name = "uk_record_game_id", columnNames = "game_id"),
        indexes = @Index(name = "idx_record_ranking", columnList = "pairs, flagged, moves, elapsed_millis"))
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 같은 게임을 두 번 등록하지 못하게 막는다 */
    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(nullable = false, length = 12)
    private String nickname;

    /** 난이도 (짝 개수) */
    @Column(nullable = false)
    private int pairs;

    @Column(nullable = false)
    private int moves;

    @Column(name = "elapsed_millis", nullable = false)
    private long elapsedMillis;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** 봇처럼 보이는 플레이. 기록은 남기되 랭킹에서는 뺀다 */
    @Column(nullable = false)
    private boolean flagged;

    /** 의심한 이유 (운영자 확인용, 클라이언트에는 보내지 않는다) */
    @Column(name = "flag_reason", length = 100)
    private String flagReason;

    protected GameRecord() {
        // JPA
    }

    public GameRecord(String gameId, String nickname, int pairs, int moves, long elapsedMillis, Instant createdAt) {
        this(gameId, nickname, pairs, moves, elapsedMillis, createdAt, null);
    }

    public GameRecord(String gameId, String nickname, int pairs, int moves, long elapsedMillis, Instant createdAt,
                      String flagReason) {
        this.gameId = gameId;
        this.nickname = nickname;
        this.pairs = pairs;
        this.moves = moves;
        this.elapsedMillis = elapsedMillis;
        this.createdAt = createdAt;
        this.flagged = flagReason != null;
        this.flagReason = flagReason;
    }

    public Long getId() { return id; }
    public String getGameId() { return gameId; }
    public String getNickname() { return nickname; }
    public int getPairs() { return pairs; }
    public int getMoves() { return moves; }
    public long getElapsedMillis() { return elapsedMillis; }
    public Instant getCreatedAt() { return createdAt; }
    public boolean isFlagged() { return flagged; }
    public String getFlagReason() { return flagReason; }
}
