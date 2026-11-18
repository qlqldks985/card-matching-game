-- 끝난 게임 한 판의 기록 (랭킹용)
CREATE TABLE game_record (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    game_id        VARCHAR(36)  NOT NULL,
    nickname       VARCHAR(12)  NOT NULL,
    pairs          INT          NOT NULL,
    moves          INT          NOT NULL,
    elapsed_millis BIGINT       NOT NULL,
    created_at     DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_record_game_id UNIQUE (game_id)
);

-- 랭킹 정렬 순서(난이도 → 시도 → 시간)와 같은 순서의 인덱스
CREATE INDEX idx_record_ranking ON game_record (pairs, moves, elapsed_millis);
