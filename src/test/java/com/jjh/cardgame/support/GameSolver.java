package com.jjh.cardgame.support;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;

import java.util.HashMap;
import java.util.Map;

/** 테스트용: 사람처럼 본 카드를 기억하며 게임을 끝까지 푼다 (그림을 미리 알 수 없으므로) */
public final class GameSolver {

    private GameSolver() {
    }

    public static void solve(Game game) {
        Map<String, Integer> seen = new HashMap<>(); // 짝을 못 찾은 채로 본 카드: 그림 -> 위치
        int next = 0;                                 // 아직 한 번도 안 뒤집은 카드
        while (!game.isFinished()) {
            int a = next++;
            FlipResult first = game.flip(a);
            Integer partner = seen.remove(first.symbol());
            if (partner != null) {
                game.flip(partner);
                continue;
            }
            int b = next++;
            FlipResult second = game.flip(b);
            if (second.matched()) {
                continue;
            }
            seen.put(first.symbol(), a);
            Integer known = seen.remove(second.symbol());
            if (known != null) {
                game.flip(b);
                game.flip(known);
            } else {
                seen.put(second.symbol(), b);
            }
        }
    }
}
