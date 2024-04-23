package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.center.dao.TransactionPointDao;
import com.jimmy.friday.center.entity.TransactionPoint;
import com.jimmy.friday.center.service.TransactionPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * (TransactionPoint)表服务实现类
 *
 * @author makejava
 * @since 2024-01-19 14:54:40
 */
@Service("transactionPointService")
public class TransactionPointServiceImpl extends ServiceImpl<TransactionPointDao, TransactionPoint> implements TransactionPointService {

    @Autowired
    private TransactionPointDao transactionPointDao;

    @Override
    public boolean updateStatus(TransactionStatusEnum update, String id, TransactionStatusEnum expect) {
        return transactionPointDao.updateStatus(update.getState(), id, expect.getState());
    }

    @Override
    public boolean updateTimeout(Integer timeout, String id, Long timeoutTimestamp) {
        return transactionPointDao.updateTimeout(timeout, id, timeoutTimestamp);
    }

    @Override
    public List<TransactionPoint> getTimeoutTransaction() {
        QueryWrapper<TransactionPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("timeout_timestamp", System.currentTimeMillis());
        queryWrapper.eq("status", TransactionStatusEnum.WAIT.getState());
        return this.list(queryWrapper);
    }

    @Override
    public List<TransactionPoint> getExpiredTransaction(Collection<String> ids) {
        QueryWrapper<TransactionPoint> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("status", TransactionStatusEnum.WAIT.getState());
        queryWrapper.in("id", ids);
        return this.list(queryWrapper);
    }
}

