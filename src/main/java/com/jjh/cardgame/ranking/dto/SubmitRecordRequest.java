package com.jjh.cardgame.ranking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SubmitRecordRequest(
        @NotBlank(message = "닉네임을 입력해 주세요.")
        @Pattern(regexp = "^\\s*[가-힣a-zA-Z0-9_]{1,12}\\s*$",
                message = "닉네임은 한글, 영문, 숫자, _ 로 1~12자까지 가능합니다.")
        String nickname) {
}
