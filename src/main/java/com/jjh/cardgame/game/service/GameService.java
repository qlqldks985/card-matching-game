package com.jjh.cardgame.game.service;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.game.dto.FlipResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    public static final int DEFAULT_PAIRS = 8;

    // Windows 10 기본 글꼴에서도 보이는 이모지만 사용 (🫐 같은 최신 이모지는 빈 네모로 표시됨)
    private static final List<String> SYMBOLS = List.of(
            "🍎", "🍌", "🍇", "🍓", "🍒", "🍑", "🍍", "🥝", "🍉", "🍋", "🥥", "🍐");

    // 1단계: 진행 중인 게임은 메모리에 보관 (2단계에서 결과를 MySQL에 저장)
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Random random = new SecureRandom();

    public Game create(Integer pairs) {
        int count = pairs != null ? pairs : DEFAULT_PAIRS;
        List<String> pool = new ArrayList<>(SYMBOLS);
        Collections.shuffle(pool, random);

        Game game = new Game(UUID.randomUUID().toString(), pool.subList(0, count), random);
        games.put(game.getId(), game);
        return game;
    }

    public FlipResponse flip(String gameId, int index) {
        Game game = find(gameId);
        FlipResult result = game.flip(index);
        return FlipResponse.of(result, game);
    }

    public Game find(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }
}
