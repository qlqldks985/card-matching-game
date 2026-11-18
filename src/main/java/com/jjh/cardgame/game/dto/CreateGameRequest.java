package com.jjh.cardgame.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** pairs: 짝의 개수 (카드 수 = pairs * 2). 비우면 기본값 8쌍(16장). */
public record CreateGameRequest(
        @Min(value = 2, message = "짝은 최소 2쌍이어야 합니다.")
        @Max(value = 12, message = "짝은 최대 12쌍까지 가능합니다.")
        Integer pairs) {
}
