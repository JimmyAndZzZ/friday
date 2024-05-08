package com.jimmy.friday.center.core.transaction;

import cn.hutool.core.collection.CollUtil;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.AckTypeEnum;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import com.jimmy.friday.boot.message.transaction.TransactionAck;
import com.jimmy.friday.boot.message.transaction.TransactionRefund;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.TransactionPoint;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.service.TransactionPointService;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.utils.RedisConstants;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("fridayTransactionManager")
public class TransactionManager {

    private static final int DEFAULT_TIME_OUT = 300;

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private TransactionPointService transactionPointService;

    public void register(TransactionFacts transactionFacts, Long traceId) {
        String applicationId = transactionFacts.getApplicationId();
        String transactionId = transactionFacts.getTransactionId();

        Channel c = ChannelHandlerPool.getChannel(applicationId);
        if (c == null) {
            log.error("当前应用id不存在:{},事务id:{}", applicationId, transactionId);
            return;
        }

        TransactionAck transactionAck = new TransactionAck();
        transactionAck.setTraceId(traceId);
        transactionAck.setTransactionId(transactionId);
        transactionAck.setAckTypeEnum(AckTypeEnum.SUCCESS);

        stripedLock.readWriteLockRead(RedisConstants.Transaction.TRANSACTION_READ_WRITE_LOCK + transactionId, 120L, TimeUnit.SECONDS, new Runnable() {
            @Override
            public void run() {
                try {
                    if (attachmentCache.setIfAbsent(RedisConstants.Transaction.TRANSACTION_POINT + transactionId, TransactionStatusEnum.WAIT.getState())) {
                        TransactionPoint transactionPoint = new TransactionPoint();
                        transactionPoint.setId(transactionId);
                        transactionPoint.setCreateDate(new Date());
                        transactionPoint.setTimeout(DEFAULT_TIME_OUT);
                        transactionPoint.setStatus(TransactionStatusEnum.WAIT.getState());
                        transactionPoint.setTimeoutTimestamp(transactionFacts.getCurrentTimestamp() + DEFAULT_TIME_OUT * 1000);
                        transactionPointService.save(transactionPoint);
                        //过期时间三天
                        attachmentCache.expire(RedisConstants.Transaction.TRANSACTION_POINT + transactionId, 3L, TimeUnit.DAYS);
                    } else {
                        String attachment = attachmentCache.attachment(RedisConstants.Transaction.TRANSACTION_POINT + transactionId);

                        TransactionStatusEnum transactionStatusEnum = TransactionStatusEnum.queryByState(attachment);
                        if (transactionStatusEnum == null) {
                            log.error("事务id:{}，状态异常,缓存事务状态:{}", transactionId, attachment);
                            transactionStatusEnum = TransactionStatusEnum.FAIL;
                        }

                        if (!TransactionStatusEnum.WAIT.equals(transactionStatusEnum)) {
                            transactionFacts.setTransactionStatus(transactionStatusEnum);
                            //事务提交退回
                            TransactionRefund transactionRefund = new TransactionRefund();
                            transactionRefund.setId(transactionId);
                            transactionRefund.setTransactionFacts(transactionFacts);
                            c.writeAndFlush(new Event(EventTypeEnum.TRANSACTION_REFUND, JsonUtil.toString(transactionRefund)));
                            return;
                        }
                    }

                    attachmentCache.attach(RedisConstants.Transaction.TRANSACTION_FACTS + transactionId, transactionFacts.getId().toString(), transactionFacts);
                } catch (Exception e) {
                    transactionAck.setAckTypeEnum(AckTypeEnum.ERROR);
                    throw e;
                } finally {
                    c.writeAndFlush(new Event(EventTypeEnum.TRANSACTION_ACK, JsonUtil.toString(transactionAck)));
                }
            }
        });
    }

    public Collection<TransactionFacts> getTransactionFacts(String id) {
        return attachmentCache.attachMap(RedisConstants.Transaction.TRANSACTION_FACTS + id, TransactionFacts.class).values();
    }

    public void removeTransactionFacts(String transactionId, List<Long> factIds) {
        if (CollUtil.isNotEmpty(factIds)) {
            for (Long factId : factIds) {
                attachmentCache.remove(RedisConstants.Transaction.TRANSACTION_FACTS + transactionId, factId.toString());
            }
        }
    }

    public void removeTransactionPoint(String transactionId) {
        attachmentCache.remove(RedisConstants.Transaction.TRANSACTION_POINT + transactionId);
    }
}
