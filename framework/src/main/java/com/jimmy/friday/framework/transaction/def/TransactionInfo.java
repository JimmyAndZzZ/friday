package com.jimmy.friday.framework.transaction.def;

import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class TransactionInfo implements Serializable {

    private String id;

    private TransactionTypeEnum transactionTypeEnum;

    private Class<?> executeClass;

    private String confirmMethod;

    private String cancelMethod;

    private Integer timeout;

    private Boolean isStart;

    private List<InvokeParam> invokeParams = new ArrayList<>();

    public void addInvokeParam(String name, String className, String jsonData) {
        this.invokeParams.add(new InvokeParam(name, className, jsonData));
    }
}
