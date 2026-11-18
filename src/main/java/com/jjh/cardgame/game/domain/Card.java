package com.jjh.cardgame.game.domain;

/** 카드 한 장. 그림(symbol)은 서버만 알고 있고, 뒤집을 때만 클라이언트에 공개된다. */
public class Card {

    private final String symbol;
    private boolean matched;

    public Card(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isMatched() {
        return matched;
    }

    void markMatched() {
        this.matched = true;
    }
}
