package com.jimmy.friday.center.action.transaction;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import com.jimmy.friday.boot.message.transaction.TransactionAck;
import com.jimmy.friday.boot.message.transaction.TransactionNotify;
import com.jimmy.friday.boot.message.transaction.TransactionSubmit;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.core.transaction.TransactionManager;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.service.TransactionPointService;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.utils.RedisConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TransactionSubmitAction implements Action<TransactionSubmit> {

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private TransactionPointService transactionPointService;

    @Override
    public void action(TransactionSubmit transactionSubmit, ChannelHandlerContext channelHandlerContext) {
        String id = transactionSubmit.getId();
        TransactionStatusEnum transactionStatus = transactionSubmit.getTransactionStatus();

        TransactionAck transactionAck = new TransactionAck();
        transactionAck.setTraceId(transactionSubmit.getTraceId());
        transactionAck.setTransactionId(id);
        transactionAck.setAckTypeEnum(AckTypeEnum.SUCCESS);

        stripedLock.readWriteLockWrite(RedisConstants.Transaction.TRANSACTION_READ_WRITE_LOCK + id, 120L, TimeUnit.SECONDS, new Runnable() {
            @Override
            public void run() {
                try {
                    if (!transactionPointService.updateStatus(transactionStatus, id, TransactionStatusEnum.WAIT)) {
                        log.error("事务状态已更新，当前提交作废:{}", id);
                        return;
                    }
                    //更新缓存
                    attachmentCache.attachString(RedisConstants.Transaction.TRANSACTION_POINT + id, transactionStatus.getState());
                    attachmentCache.expire(RedisConstants.Transaction.TRANSACTION_POINT + id, 3L, TimeUnit.DAYS);

                    Collection<TransactionFacts> transactionFacts = transactionManager.getTransactionFacts(id);
                    if (CollUtil.isEmpty(transactionFacts)) {
                        log.error("当前事务上下文不存在:{}", id);
                        return;
                    }
                    //groupby
                    Map<String, List<TransactionFacts>> groupBy = transactionFacts.stream().collect(Collectors.groupingBy(TransactionFacts::getApplicationId));

                    for (Map.Entry<String, List<TransactionFacts>> entry : groupBy.entrySet()) {
                        String key = entry.getKey();
                        List<TransactionFacts> value = entry.getValue();

                        Channel c = ChannelHandlerPool.getChannel(key);
                        if (c == null) {
                            log.error("当前应用id不存在:{},事务id:{}", key, id);
                            continue;
                        }

                        TransactionNotify transactionNotify = new TransactionNotify();
                        transactionNotify.setId(id);
                        transactionNotify.setTransactionFacts(value);
                        transactionNotify.setTransactionStatus(transactionStatus);
                        c.writeAndFlush(new Event(EventTypeEnum.TRANSACTION_NOTIFY, JsonUtil.toString(transactionNotify)));
                    }
                } catch (Exception e) {
                    transactionAck.setAckTypeEnum(AckTypeEnum.ERROR);
                    throw e;
                } finally {
                    channelHandlerContext.writeAndFlush(new Event(EventTypeEnum.TRANSACTION_ACK, JsonUtil.toString(transactionAck)));
                }
            }
        });
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_SUBMIT;
    }
}
