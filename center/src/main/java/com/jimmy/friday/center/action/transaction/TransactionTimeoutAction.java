package com.jimmy.friday.center.action.transaction;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.TransactionStatusEnum;
import com.jimmy.friday.boot.message.transaction.TransactionSubmit;
import com.jimmy.friday.boot.message.transaction.TransactionTimeout;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.entity.TransactionPoint;
import com.jimmy.friday.center.service.TransactionPointService;
import com.jimmy.friday.center.utils.RedisConstants;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class TransactionTimeoutAction implements Action<TransactionTimeout>, Initialize {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private TransactionPointService transactionPointService;

    @Autowired
    private TransactionSubmitAction transactionSubmitAction;

    @Override
    public void action(TransactionTimeout transactionTimeout, ChannelHandlerContext channelHandlerContext) {
        String id = transactionTimeout.getId();
        Integer timeout = transactionTimeout.getTimeout();
        Long currentTimestamp = transactionTimeout.getCurrentTimestamp();

        transactionPointService.updateTimeout(timeout, id, currentTimestamp + timeout * 1000);
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_TIMEOUT;
    }

    @Override
    public void init() throws Exception {
        CronUtil.schedule(IdUtil.simpleUUID(), "*/30 * * * * *", () -> {
            Set<String> process = Sets.newHashSet();

            List<TransactionPoint> timeoutTransaction = transactionPointService.getTimeoutTransaction();
            if (CollUtil.isNotEmpty(timeoutTransaction)) {
                for (TransactionPoint transactionPoint : timeoutTransaction) {
                    if (process.add(transactionPoint.getId())) {
                        TransactionSubmit transactionSubmit = new TransactionSubmit();
                        transactionSubmit.setId(transactionPoint.getId());
                        transactionSubmit.setTransactionStatus(TransactionStatusEnum.TIMEOUT);
                        transactionSubmitAction.action(transactionSubmit, null);
                    }
                }
            }

            Iterable<String> factsKeys = attachmentCache.keys(RedisConstants.TRANSACTION_FACTS + "*");
            if (CollUtil.isNotEmpty(factsKeys)) {
                List<String> ids = Lists.newArrayList();
                for (String factsKey : factsKeys) {
                    ids.add(StrUtil.removeAll(factsKey, RedisConstants.TRANSACTION_FACTS));
                }

                List<TransactionPoint> expiredTransactions = transactionPointService.getExpiredTransaction(ids);
                if (CollUtil.isNotEmpty(expiredTransactions)) {
                    for (TransactionPoint expiredTransaction : expiredTransactions) {
                        TransactionStatusEnum transactionStatusEnum = TransactionStatusEnum.queryByState(expiredTransaction.getStatus());
                        if (transactionStatusEnum != null) {
                            TransactionSubmit transactionSubmit = new TransactionSubmit();
                            transactionSubmit.setId(expiredTransaction.getId());
                            transactionSubmit.setTransactionStatus(transactionStatusEnum);
                            transactionSubmitAction.action(transactionSubmit, null);
                        }
                    }
                }
            }
        });
    }

    @Override
    public int sort() {
        return 1;
    }
}
