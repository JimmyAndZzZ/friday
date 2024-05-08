package com.jimmy.friday.boot.enums.gateway;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING("0", "等待支付"),
    PROCESSING("1", "正在支付"),
    PAID("2", "支付成功"),
    CANCELED("3", "取消支付"),
    REFUNDED("4", "已退款"),
    CLOSE("5", "已关闭"),
    APPLY_REFUND("6", "申请退款"),
    REFUNDING("7", "正在退款"),
    REFUND_FAIL("8", "退款失败"),
    ABNORMAL("9", "支付异常");

    private String code;

    private String message;

    public static OrderStatusEnum queryByCode(String code) {
        for (OrderStatusEnum value : OrderStatusEnum.values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }

        return null;
    }

}
