package com.jjh.cardgame.game;

import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class GameTimingTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-18T00:00:00Z"));

    @Test
    void 시간은_첫_카드를_뒤집을_때부터_잰다() {
        Game game = new Game("t", List.of("A"), new Random(1), clock);
        clock.advance(Duration.ofSeconds(30));          // 판만 열어두고 30초 딴짓
        assertThat(game.elapsed()).isZero();

        game.flip(0);
        clock.advance(Duration.ofSeconds(5));
        game.flip(1);                                    // 한 쌍뿐이라 바로 끝남
        clock.advance(Duration.ofSeconds(100));

        assertThat(game.isFinished()).isTrue();
        assertThat(game.elapsed()).isEqualTo(Duration.ofSeconds(5));
    }

    @Test
    void 마지막_활동_뒤로_시간이_지나면_방치된_게임이다() {
        Game game = new Game("t", List.of("A", "B"), new Random(1), clock);
        clock.advance(Duration.ofMinutes(20));
        game.flip(0);
        clock.advance(Duration.ofMinutes(29));
        assertThat(game.isIdleFor(Duration.ofMinutes(30))).isFalse();
        clock.advance(Duration.ofMinutes(1));
        assertThat(game.isIdleFor(Duration.ofMinutes(30))).isTrue();
    }
}
