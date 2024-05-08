package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import com.jimmy.friday.center.entity.TransactionPoint;

import java.util.Collection;
import java.util.List;

/**
 * (TransactionPoint)表服务接口
 *
 * @author makejava
 * @since 2024-01-19 14:54:40
 */
public interface TransactionPointService extends IService<TransactionPoint> {

    List<TransactionPoint> getExpiredTransaction(Collection<String> ids);

    List<TransactionPoint> getTimeoutTransaction();

    boolean updateStatus(TransactionStatusEnum update, String id, TransactionStatusEnum expect);

    boolean updateTimeout(Integer timeout, String id, Long timeoutTimestamp);

}

