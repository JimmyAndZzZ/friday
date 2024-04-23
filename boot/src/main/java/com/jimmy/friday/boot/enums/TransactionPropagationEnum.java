package com.jimmy.friday.boot.enums;

public enum TransactionPropagationEnum {
    REQUIRED,

    SUPPORTS;

    public static TransactionPropagationEnum parser(String code) {
        if (code.equals("SUPPORTS")) {
            return SUPPORTS;
        }

        return REQUIRED;
    }

}
