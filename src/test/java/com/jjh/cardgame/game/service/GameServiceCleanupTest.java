package com.jjh.cardgame.game.service;

import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceCleanupTest {

    @Test
    void 방치된_게임만_메모리에서_지운다() {
        GameService service = new GameService();
        MutableClock clock = new MutableClock(Instant.parse("2026-11-18T00:00:00Z"));
        Game old = new Game("old", List.of("A"), new Random(1), clock);
        service.register(old);
        clock.advance(GameService.IDLE_TIMEOUT);
        Game fresh = service.create(2);                  // 실제 시계를 쓰는 새 게임

        assertThat(service.removeIdleGames()).isEqualTo(1);
        assertThat(service.activeGameCount()).isEqualTo(1);
        assertThat(service.find(fresh.getId())).isSameAs(fresh);
        assertThatThrownBy(() -> service.find("old")).isInstanceOf(GameNotFoundException.class);
    }
}
