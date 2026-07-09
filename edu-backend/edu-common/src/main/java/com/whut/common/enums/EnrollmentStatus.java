package com.whut.common.enums;

public enum EnrollmentStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "已选课"),
    DROPPED(2, "已退选"),
    REJECTED(4, "审核不通过");

    private final int code;
    private final String description;

    EnrollmentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
