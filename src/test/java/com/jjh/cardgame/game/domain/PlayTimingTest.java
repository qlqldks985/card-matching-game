package com.jjh.cardgame.game.domain;

import com.jjh.cardgame.support.GameSolver;
import com.jjh.cardgame.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class PlayTimingTest {

    private static Duration ms(long millis) {
        return Duration.ofMillis(millis);
    }

    @Test
    void 화면의_대기_시간을_지키면_의심하지_않는다() {
        PlayTiming timing = new PlayTiming();
        timing.record(null, false);         // 첫 카드
        timing.record(ms(300), false);      // 두 번째 카드 (틀림)
        timing.record(ms(850), true);       // 틀린 뒤 0.8초 대기 + 네트워크
        timing.record(ms(120), false);      // 빠르지만 사람 손으로 가능한 속도
        assertThat(timing.suspicionReason()).isEmpty();
    }

    @Test
    void 틀린_직후_대기_시간_경계값() {
        PlayTiming timing = new PlayTiming();
        timing.record(ms(700), true);       // 정확히 기준값은 통과
        timing.record(ms(700), true);
        assertThat(timing.suspicionReason()).isEmpty();

        timing.record(ms(699), true);
        assertThat(timing.suspicionReason()).as("한 번은 봐준다").isEmpty();
        timing.record(ms(699), true);
        assertThat(timing.suspicionReason()).hasValue("MISMATCH_COOLDOWN x2");
    }

    @Test
    void 사람_손으로_불가능한_간격이_세_번이면_의심한다() {
        PlayTiming timing = new PlayTiming();
        timing.record(ms(79), false);
        timing.record(ms(10), false);
        assertThat(timing.suspicionReason()).isEmpty();
        timing.record(ms(0), false);
        assertThat(timing.suspicionReason()).hasValue("FAST_FLIP x3");
    }

    @Test
    void 사람_속도로_푼_게임은_통과하고_곧바로_푼_게임은_걸린다() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-18T00:00:00Z"));
        List<String> symbols = List.of("A", "B", "C", "D", "E", "F");

        Game human = new Game("human", symbols, new Random(7), clock);
        GameSolver.solve(human, clock, Duration.ofSeconds(1));
        assertThat(human.suspicionReason()).isEmpty();

        Game bot = new Game("bot", symbols, new Random(7), clock);
        GameSolver.solve(bot);
        assertThat(bot.isFinished()).isTrue();
        assertThat(bot.suspicionReason()).hasValueSatisfying(reason ->
                assertThat(reason).contains("MISMATCH_COOLDOWN").contains("FAST_FLIP"));
    }
}
