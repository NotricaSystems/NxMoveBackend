package com.next.move.enums;

public enum GoalStatus {
    CREATED(0),
    STARTED(1),
    PAUSED(2),
    OVERDUE(3),
    ACHIEVED(4),
    ABORTED(5);
    private final int code;
    GoalStatus(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}

