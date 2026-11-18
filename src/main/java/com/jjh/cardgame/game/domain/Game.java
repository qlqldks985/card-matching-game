package com.jjh.cardgame.game.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** 게임 한 판. 카드 배치와 진행 상태를 모두 서버에서 관리한다. */
public class Game {

    private final String id;
    private final List<Card> cards;
    private final Instant startedAt = Instant.now();
    private Instant finishedAt;

    private Integer firstFlippedIndex; // 이번 턴에 먼저 뒤집은 카드
    private int moves;                 // 두 장을 비교한 횟수
    private int matchedPairs;

    public Game(String id, List<String> symbols, Random random) {
        this.id = id;
        List<Card> deck = new ArrayList<>();
        for (String symbol : symbols) {
            deck.add(new Card(symbol));
            deck.add(new Card(symbol));
        }
        Collections.shuffle(deck, random);
        this.cards = List.copyOf(deck);
    }

    public synchronized FlipResult flip(int index) {
        if (isFinished()) {
            throw new IllegalStateException("이미 끝난 게임입니다.");
        }
        if (index < 0 || index >= cards.size()) {
            throw new IllegalArgumentException("잘못된 카드 위치입니다: " + index);
        }
        Card card = cards.get(index);
        if (card.isMatched()) {
            throw new IllegalArgumentException("이미 짝을 맞춘 카드입니다.");
        }

        // 첫 번째 카드
        if (firstFlippedIndex == null) {
            firstFlippedIndex = index;
            return new FlipResult(index, card.getSymbol(), null, null);
        }

        // 두 번째 카드
        if (firstFlippedIndex == index) {
            throw new IllegalArgumentException("같은 카드를 두 번 뒤집을 수 없습니다.");
        }
        int firstIndex = firstFlippedIndex;
        Card first = cards.get(firstIndex);
        firstFlippedIndex = null;
        moves++;

        boolean matched = first.getSymbol().equals(card.getSymbol());
        if (matched) {
            first.markMatched();
            card.markMatched();
            matchedPairs++;
            if (matchedPairs == totalPairs()) {
                finishedAt = Instant.now();
            }
        }
        return new FlipResult(index, card.getSymbol(), firstIndex, matched);
    }

    public String getId() {
        return id;
    }

    public int totalCards() {
        return cards.size();
    }

    public int totalPairs() {
        return cards.size() / 2;
    }

    public synchronized int getMoves() {
        return moves;
    }

    public synchronized int getMatchedPairs() {
        return matchedPairs;
    }

    public synchronized boolean isFinished() {
        return finishedAt != null;
    }

    /** 게임 시작부터 종료(진행 중이면 현재)까지 걸린 시간 */
    public synchronized Duration elapsed() {
        return Duration.between(startedAt, finishedAt != null ? finishedAt : Instant.now());
    }
}
