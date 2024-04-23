package com.jimmy.friday.framework.transaction;

import com.jimmy.friday.boot.enums.TransactionTypeEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import lombok.Getter;
import lombok.Setter;

public class TransactionContext {

    private final static ThreadLocal<TransactionContext> HOLDER = new ThreadLocal<>();

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private TransactionTypeEnum transactionTypeEnum;

    public static TransactionContext setTransactionType(TransactionTypeEnum transactionTypeEnum) {
        HOLDER.get().setTransactionTypeEnum(transactionTypeEnum);
        return HOLDER.get();
    }

    public static TransactionContext create(TransactionTypeEnum transactionTypeEnum) {
        TransactionContext transactionContext = new TransactionContext();
        transactionContext.setId(ShortUUID.uuid());
        transactionContext.setTransactionTypeEnum(transactionTypeEnum);
        HOLDER.set(transactionContext);
        return HOLDER.get();
    }

    public void release() {
        this.transactionTypeEnum = null;
    }

    public static TransactionContext get() {
        return HOLDER.get();
    }

    public static boolean isNull() {
        return HOLDER.get() == null;
    }

    public static void clear() {
        HOLDER.remove();
    }
}
