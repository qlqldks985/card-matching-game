package com.jjh.cardgame.game.dto;

import jakarta.validation.constraints.NotNull;

public record FlipRequest(@NotNull(message = "index는 필수입니다.") Integer index) {
}
