package com.jjh.cardgame.game;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameTest {

    private Game game;
    private int[] pairA;   // "A" 그림 두 장의 위치
    private int[] pairB;   // "B" 그림 두 장의 위치

    @BeforeEach
    void setUp() {
        game = new Game("test", List.of("A", "B"), new Random(42));
        pairA = positionsOf("A");
        pairB = positionsOf("B");
    }

    /** 섞인 카드에서 그림 위치를 찾는다 (테스트 전용: 실제로 뒤집어 보고 되돌릴 수 없으므로 새 게임으로 탐색) */
    private int[] positionsOf(String symbol) {
        List<Integer> found = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Game probe = new Game("probe", List.of("A", "B"), new Random(42));
            if (probe.flip(i).symbol().equals(symbol)) found.add(i);
        }
        return new int[]{found.get(0), found.get(1)};
    }

    @Test
    void 첫_번째_카드는_비교하지_않는다() {
        FlipResult r = game.flip(pairA[0]);
        assertThat(r.matched()).isNull();
        assertThat(r.pairedWith()).isNull();
        assertThat(game.getMoves()).isZero();
    }

    @Test
    void 같은_그림이면_짝이_맞는다() {
        game.flip(pairA[0]);
        FlipResult r = game.flip(pairA[1]);
        assertThat(r.matched()).isTrue();
        assertThat(r.pairedWith()).isEqualTo(pairA[0]);
        assertThat(game.getMatchedPairs()).isEqualTo(1);
        assertThat(game.getMoves()).isEqualTo(1);
    }

    @Test
    void 다른_그림이면_짝이_맞지_않는다() {
        game.flip(pairA[0]);
        FlipResult r = game.flip(pairB[0]);
        assertThat(r.matched()).isFalse();
        assertThat(game.getMatchedPairs()).isZero();
    }

    @Test
    void 모든_짝을_맞추면_게임이_끝난다() {
        game.flip(pairA[0]);
        game.flip(pairA[1]);
        game.flip(pairB[0]);
        game.flip(pairB[1]);
        assertThat(game.isFinished()).isTrue();
        assertThatThrownBy(() -> game.flip(0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 같은_카드를_두_번_뒤집을_수_없다() {
        game.flip(pairA[0]);
        assertThatThrownBy(() -> game.flip(pairA[0])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 이미_맞춘_카드는_다시_뒤집을_수_없다() {
        game.flip(pairA[0]);
        game.flip(pairA[1]);
        assertThatThrownBy(() -> game.flip(pairA[0])).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 범위를_벗어난_위치는_거부한다() {
        assertThatThrownBy(() -> game.flip(4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> game.flip(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
