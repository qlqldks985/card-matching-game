package com.jjh.cardgame.ranking;

import com.jjh.cardgame.ranking.domain.GameRecord;
import com.jjh.cardgame.ranking.domain.GameRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
// 기본 내장 H2 로 바꿔 끼우지 않고, 설정 파일의 H2(MySQL 모드)를 써서 Flyway SQL 을 그대로 검증한다
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameRecordRepositoryTest {

    @Autowired
    GameRecordRepository repository;

    // 같은 메모리 DB 를 다른 테스트 컨텍스트와 함께 쓰므로, 남아 있는 기록을 지우고 시작한다 (테스트 끝나면 롤백)
    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private GameRecord save(String nickname, int pairs, int moves, long millis) {
        return repository.saveAndFlush(new GameRecord(UUID.randomUUID().toString(), nickname, pairs, moves, millis,
                Instant.parse("2026-11-18T00:00:00Z")));
    }

    @Test
    void 시도_횟수_다음_시간_다음_등록_순서로_정렬한다() {
        save("slow", 8, 10, 50_000);
        save("fast", 8, 10, 30_000);
        save("best", 8, 9, 90_000);
        save("tieLater", 8, 10, 30_000);   // fast 와 완전히 같지만 나중에 등록
        save("otherLevel", 6, 1, 1_000);   // 다른 난이도는 섞이지 않음

        List<String> names = repository.findByPairsAndFlaggedFalseOrderByMovesAscElapsedMillisAscIdAsc(8, PageRequest.of(0, 10))
                .stream().map(GameRecord::getNickname).toList();

        assertThat(names).containsExactly("best", "fast", "tieLater", "slow");
    }

    @Test
    void 순위_계산은_정렬_순서와_같다() {
        save("best", 8, 9, 90_000);
        GameRecord fast = save("fast", 8, 10, 30_000);
        GameRecord tie = save("tieLater", 8, 10, 30_000);
        GameRecord slow = save("slow", 8, 10, 50_000);
        save("otherLevel", 6, 1, 1_000);

        assertThat(rank(fast)).isEqualTo(2);
        assertThat(rank(tie)).isEqualTo(3);
        assertThat(rank(slow)).isEqualTo(4);
    }

    @Test
    void 조회_개수를_제한한다() {
        for (int i = 0; i < 5; i++) {
            save("p" + i, 8, 10 + i, 1_000);
        }
        assertThat(repository.findByPairsAndFlaggedFalseOrderByMovesAscElapsedMillisAscIdAsc(8, PageRequest.of(0, 3))).hasSize(3);
    }

    @Test
    void 봇_의심_기록은_랭킹과_순위_계산에서_빠진다() {
        repository.saveAndFlush(new GameRecord(UUID.randomUUID().toString(), "bot", 8, 1, 10,
                Instant.parse("2026-11-18T00:00:00Z"), "FAST_FLIP x9"));
        GameRecord human = save("human", 8, 12, 20_000);

        assertThat(repository.findByPairsAndFlaggedFalseOrderByMovesAscElapsedMillisAscIdAsc(8, PageRequest.of(0, 10)))
                .extracting(GameRecord::getNickname).containsExactly("human");
        assertThat(rank(human)).as("봇이 더 잘했어도 1위").isEqualTo(1);
    }

    private long rank(GameRecord r) {
        return repository.countAhead(r.getPairs(), r.getMoves(), r.getElapsedMillis(), r.getId()) + 1;
    }
}
