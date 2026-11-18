package com.jjh.cardgame.ranking.service;

public class DuplicateRecordException extends RuntimeException {
    public DuplicateRecordException() {
        super("이미 기록을 등록한 게임입니다.");
    }
}
