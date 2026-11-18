package com.jjh.cardgame.game.service;

import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.support.MutableClock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceCleanupTest {

    @Test
    void 방치된_게임만_메모리에서_지운다() {
        MutableClock clock = new MutableClock(Instant.parse("2026-11-18T00:00:00Z"));
        GameService service = new GameService(clock);
        Game old = service.create(2);
        clock.advance(GameService.IDLE_TIMEOUT);
        Game fresh = service.create(2);

        assertThat(service.removeIdleGames()).isEqualTo(1);
        assertThat(service.activeGameCount()).isEqualTo(1);
        assertThat(service.find(fresh.getId())).isSameAs(fresh);
        assertThatThrownBy(() -> service.find(old.getId())).isInstanceOf(GameNotFoundException.class);
    }
}
