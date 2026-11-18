package com.jjh.cardgame.game.dto;

import com.jjh.cardgame.game.domain.FlipResult;
import com.jjh.cardgame.game.domain.Game;

public record FlipResponse(
        int index,
        String symbol,
        Integer pairedWith,
        Boolean matched,
        int moves,
        int matchedPairs,
        int totalPairs,
        boolean finished,
        long elapsedSeconds) {

    public static FlipResponse of(FlipResult result, Game game) {
        return new FlipResponse(
                result.index(),
                result.symbol(),
                result.pairedWith(),
                result.matched(),
                game.getMoves(),
                game.getMatchedPairs(),
                game.totalPairs(),
                game.isFinished(),
                game.elapsed().toSeconds());
    }
}
