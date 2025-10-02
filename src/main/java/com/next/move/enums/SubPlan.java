package com.next.move.enums;

public enum SubPlan {
    NOPLAN(-1),
    FREE(0),
    PERSONAL(1),
    ENTERPRISE(2);
    private final int code;
    SubPlan(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}



