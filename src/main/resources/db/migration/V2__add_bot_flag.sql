-- 봇 탐지: 의심 기록은 남기되 랭킹에서 뺀다
ALTER TABLE game_record ADD COLUMN flagged BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE game_record ADD COLUMN flag_reason VARCHAR(100);

-- 랭킹 조회가 flagged = false 조건을 쓰므로 인덱스에 넣는다 (난이도 → 의심 여부 → 시도 → 시간)
DROP INDEX idx_record_ranking ON game_record;
CREATE INDEX idx_record_ranking ON game_record (pairs, flagged, moves, elapsed_millis);
