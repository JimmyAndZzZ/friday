package com.jimmy.friday.boot.core.transaction;

import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionFacts implements Serializable {

    private Long id;

    private String dsName;

    private String cancelMethod;

    private String applicationId;

    private String transactionId;

    private String confirmMethod;

    private Long currentTimestamp;

    private String executeClass;

    private TransactionTypeEnum transactionType;

    private TransactionStatusEnum transactionStatus;

    private List<InvokeParam> invokeParams = new ArrayList<>();

    public TransactionFacts() {
        this.currentTimestamp = System.currentTimeMillis();
    }
}
