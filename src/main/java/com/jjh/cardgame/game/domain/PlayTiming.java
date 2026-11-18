package com.jjh.cardgame.game.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 카드를 뒤집은 간격을 보고 사람이 아닌 플레이(봇, 매크로)를 가려낸다.
 *
 * <p>화면(app.js)은 틀린 두 장을 MISMATCH_DELAY_MS(800ms) 동안 보여 주고 그동안 클릭을 막는다.
 * 그래서 정상적인 화면으로는 "틀린 직후 800ms 안에 다음 카드"가 서버에 도착할 수 없다.
 * API 를 직접 부르는 봇만 이 규칙을 어기게 된다. 시각은 모두 서버 시계로 재므로 조작할 수 없다.
 *
 * <p>네트워크나 기기 탓으로 정상 플레이가 걸리지 않도록 기준에 여유를 두고, 여러 번 어겼을 때만 의심한다.
 */
public class PlayTiming {

    /** 틀린 직후 다음 카드까지 최소 간격 (화면은 800ms, 100ms 여유) */
    static final Duration MISMATCH_COOLDOWN = Duration.ofMillis(700);
    /** 사람이 마우스나 손가락으로 카드 두 장을 누를 수 있는 최소 간격 */
    static final Duration HUMAN_MIN_INTERVAL = Duration.ofMillis(80);

    static final int COOLDOWN_VIOLATION_LIMIT = 2;
    static final int FAST_FLIP_LIMIT = 3;

    private int cooldownViolations; // 틀린 직후 너무 빨리 뒤집은 횟수
    private int fastFlips;          // 사람 손으로 불가능할 만큼 빨리 뒤집은 횟수

    /**
     * 뒤집기 한 번을 기록한다.
     *
     * @param gap           직전 뒤집기와의 간격 (첫 뒤집기면 null)
     * @param afterMismatch 직전 뒤집기가 짝이 틀린 두 번째 카드였는지
     */
    void record(Duration gap, boolean afterMismatch) {
        if (gap == null) {
            return;
        }
        if (afterMismatch && gap.compareTo(MISMATCH_COOLDOWN) < 0) {
            cooldownViolations++;
        }
        if (gap.compareTo(HUMAN_MIN_INTERVAL) < 0) {
            fastFlips++;
        }
    }

    /** 의심스러우면 그 이유를, 아니면 빈 값을 돌려준다 */
    public Optional<String> suspicionReason() {
        List<String> reasons = new ArrayList<>();
        if (cooldownViolations >= COOLDOWN_VIOLATION_LIMIT) {
            reasons.add("MISMATCH_COOLDOWN x" + cooldownViolations);
        }
        if (fastFlips >= FAST_FLIP_LIMIT) {
            reasons.add("FAST_FLIP x" + fastFlips);
        }
        return reasons.isEmpty() ? Optional.empty() : Optional.of(String.join(", ", reasons));
    }
}
