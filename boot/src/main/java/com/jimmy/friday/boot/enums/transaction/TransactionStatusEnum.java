package com.jimmy.friday.boot.enums.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionStatusEnum {

    WAIT("0"),
    SUCCESS("1"),
    FAIL("2"),
    INTERRUPT("3"),
    TIMEOUT("4"),
    COMPLETE("5");

    private String state;

    public static TransactionStatusEnum queryByState(String state) {
        for (TransactionStatusEnum value : TransactionStatusEnum.values()) {
            if (value.state.equalsIgnoreCase(state)) {
                return value;
            }
        }

        return null;
    }
}
