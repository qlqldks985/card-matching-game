package com.jjh.cardgame.game.domain;

/**
 * 카드를 한 장 뒤집은 결과.
 *
 * @param pairedWith 이번이 두 번째 카드라면 함께 비교한 첫 번째 카드의 위치, 첫 번째 카드라면 null
 * @param matched    두 번째 카드일 때 짝이 맞았는지 여부, 첫 번째 카드라면 null
 */
public record FlipResult(int index, String symbol, Integer pairedWith, Boolean matched) {
}
