package com.jjh.cardgame.support;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/** 테스트용: 사람처럼 본 카드를 기억하며 게임을 끝까지 푼다 (그림을 미리 알 수 없으므로) */
public final class GameSolver {

    private GameSolver() {
    }

    /** 시간 흐름 없이 곧바로 푼다 (봇처럼 보인다) */
    public static void solve(Game game) {
        solve(game, () -> { });
    }

    /** 카드를 뒤집기 전마다 시계를 gap 만큼 흘린다 (1초면 사람처럼 보인다) */
    public static void solve(Game game, MutableClock clock, Duration gap) {
        solve(game, () -> clock.advance(gap));
    }

    private static void solve(Game game, Runnable beforeFlip) {
        Map<String, Integer> seen = new HashMap<>(); // 짝을 못 찾은 채로 본 카드: 그림 -> 위치
        int next = 0;                                 // 아직 한 번도 안 뒤집은 카드
        while (!game.isFinished()) {
            int a = next++;
            FlipResult first = flip(game, a, beforeFlip);
            Integer partner = seen.remove(first.symbol());
            if (partner != null) {
                flip(game, partner, beforeFlip);
                continue;
            }
            int b = next++;
            FlipResult second = flip(game, b, beforeFlip);
            if (second.matched()) {
                continue;
            }
            seen.put(first.symbol(), a);
            Integer known = seen.remove(second.symbol());
            if (known != null) {
                flip(game, b, beforeFlip);
                flip(game, known, beforeFlip);
            } else {
                seen.put(second.symbol(), b);
            }
        }
    }

    private static FlipResult flip(Game game, int index, Runnable beforeFlip) {
        beforeFlip.run();
        return game.flip(index);
    }
}
