package com.jimmy.friday.center.core.gateway;

import com.jimmy.friday.center.entity.GatewayInvokeTrace;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.service.GatewayInvokeTraceService;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.utils.RabbitMQConstants;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class InvokeDelayedComponent {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private GatewayInvokeTraceService gatewayInvokeTraceService;

    public void sendDelayedMessage(Long id, Integer delay, Date invokeDate, BigDecimal cost, String appId) {
        Delayed delayed = new Delayed();
        delayed.setId(id);
        delayed.setCost(cost);
        delayed.setAppId(appId);
        delayed.setSendDate(invokeDate);

        MessageProperties properties = new MessageProperties();
        properties.setDelay((delay + 30) * 1000); // 设置消息的延迟时间，单位为毫秒

        Message message = MessageBuilder.withBody(JsonUtil.toString(delayed).getBytes(StandardCharsets.UTF_8)).andProperties(properties).build();

        rabbitTemplate.send(RabbitMQConstants.DELAYED_QUEUE_NAME, message);
    }

    @RabbitListener(queues = RabbitMQConstants.DLX_QUEUE_NAME)
    public void receiveDelayedMessage(Channel channel, Message message) throws IOException {
        MessageProperties messageProperties = message.getMessageProperties();
        byte[] body = message.getBody();
        try {
            String json = new String(body, StandardCharsets.UTF_8);

            Delayed delayed = JsonUtil.parseObject(json, Delayed.class);
            if (delayed == null) {
                log.error("json解析失败:{}", json);
                return;
            }

            GatewayInvokeTrace gatewayInvokeTrace = gatewayInvokeTraceService.getById(delayed.getId());
            if (gatewayInvokeTrace == null) {
                log.info("id:{},appId:{}准备回滚调用费用，调用时间:{},调用费用:{}", delayed.getId(), delayed.getAppId(), delayed.getSendDate(), delayed.getCost());
                gatewayAccountService.rollbackBalance(delayed.getCost(), delayed.getAppId());
                channel.basicAck(messageProperties.getDeliveryTag(), false);
            }

        } catch (Exception e) {
            channel.basicNack(messageProperties.getDeliveryTag(), false, true);
        }
    }

    @Data
    private static class Delayed implements Serializable {
        private Long id;

        private Date sendDate;

        private String appId;

        private BigDecimal cost;
    }

}
