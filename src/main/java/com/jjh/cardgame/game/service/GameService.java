package com.jjh.cardgame.game.service;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;
import com.jjh.cardgame.game.dto.FlipResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
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

    /** 이 시간 동안 아무 카드도 뒤집지 않은 게임은 메모리에서 지운다 (끝난 게임 포함) */
    static final Duration IDLE_TIMEOUT = Duration.ofMinutes(30);

    // Windows 10 기본 글꼴에서도 보이는 이모지만 사용 (🫐 같은 최신 이모지는 빈 네모로 표시됨)
    private static final List<String> SYMBOLS = List.of(
            "🍎", "🍌", "🍇", "🍓", "🍒", "🍑", "🍍", "🥝", "🍉", "🍋", "🥥", "🍐");

    // 진행 중인 게임은 메모리에 보관하고, 끝난 게임의 결과만 DB에 저장한다
    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Random random = new SecureRandom();
    private final Clock clock;

    public GameService(Clock clock) {
        this.clock = clock;
    }

    public Game create(Integer pairs) {
        int count = pairs != null ? pairs : DEFAULT_PAIRS;
        List<String> pool = new ArrayList<>(SYMBOLS);
        Collections.shuffle(pool, random);

        Game game = new Game(UUID.randomUUID().toString(), pool.subList(0, count), random, clock);
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

    public void remove(String gameId) {
        games.remove(gameId);
    }

    /** 1분마다 방치된 게임을 정리한다. 지운 개수를 돌려준다. */
    @Scheduled(fixedDelay = 60_000)
    public int removeIdleGames() {
        int before = games.size();
        games.values().removeIf(game -> game.isIdleFor(IDLE_TIMEOUT));
        return before - games.size();
    }

    int activeGameCount() {
        return games.size();
    }
}
