package com.jimmy.friday.center.action.transaction;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.Event;
import com.jimmy.friday.boot.core.transaction.TransactionFacts;
import com.jimmy.friday.boot.enums.EventTypeEnum;
import com.jimmy.friday.boot.enums.transaction.TransactionStatusEnum;
import com.jimmy.friday.boot.message.transaction.TransactionCompensation;
import com.jimmy.friday.boot.message.transaction.TransactionNotify;
import com.jimmy.friday.center.base.Action;
import com.jimmy.friday.center.core.transaction.TransactionManager;
import com.jimmy.friday.center.entity.TransactionPoint;
import com.jimmy.friday.center.netty.ChannelHandlerPool;
import com.jimmy.friday.center.service.TransactionPointService;
import com.jimmy.friday.center.utils.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TransactionCompensationAction implements Action<TransactionCompensation> {

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private TransactionPointService transactionPointService;

    @Override
    public void action(TransactionCompensation transactionCompensation, ChannelHandlerContext channelHandlerContext) {
        String applicationId = transactionCompensation.getApplicationId();
        String transactionId = transactionCompensation.getTransactionId();

        log.info("收到事务补偿请求:{}", transactionId);

        TransactionPoint byId = transactionPointService.getById(transactionId);
        if (byId == null) {
            log.error("事务不存在:{}", transactionId);
            return;
        }

        TransactionStatusEnum transactionStatusEnum = TransactionStatusEnum.queryByState(byId.getStatus());
        if (transactionStatusEnum == null) {
            log.error("事务状态为空:{}", transactionId);
            return;
        }

        if (transactionStatusEnum.equals(TransactionStatusEnum.WAIT)) {
            log.error("事务未提交:{}", transactionId);
            return;
        }

        Collection<TransactionFacts> transactionFacts = transactionManager.getTransactionFacts(transactionId);
        if (CollUtil.isNotEmpty(transactionFacts)) {
            List<TransactionFacts> collect = transactionFacts.stream().filter(bean -> bean.getApplicationId().equals(applicationId)).collect(Collectors.toList());

            if (CollUtil.isNotEmpty(collect)) {
                this.transactionCompensation(applicationId, transactionId, transactionStatusEnum, collect);
                return;
            }
        }

        log.error("当前应用事务上下文不存在:{}", transactionId);

        this.transactionCompensation(applicationId, transactionId, transactionStatusEnum, Lists.newArrayList());
    }

    @Override
    public EventTypeEnum type() {
        return EventTypeEnum.TRANSACTION_COMPENSATION;
    }

    /**
     * 事务补偿通知
     *
     * @param applicationId
     * @param transactionId
     * @param transactionStatusEnum
     * @param collect
     */
    private void transactionCompensation(String applicationId, String transactionId, TransactionStatusEnum transactionStatusEnum, List<TransactionFacts> collect) {
        Channel c = ChannelHandlerPool.getChannel(applicationId);
        if (c == null) {
            log.error("当前应用id不存在:{},事务id:{}", applicationId, transactionId);
            return;
        }

        TransactionNotify transactionNotify = new TransactionNotify();
        transactionNotify.setId(transactionId);
        transactionNotify.setTransactionFacts(collect);
        transactionNotify.setTransactionStatus(transactionStatusEnum);
        c.writeAndFlush(new Event(EventTypeEnum.TRANSACTION_NOTIFY, JsonUtil.toString(transactionNotify)));
    }
}
