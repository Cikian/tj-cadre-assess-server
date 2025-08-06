package org.jeecg.modules.enums;


public enum CommonEnum {
    REGULAR("regular"),
    BUSINESS("business"),
    REPORT("report"),
    ANNUAL("annual");

    private final String value;

    CommonEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
