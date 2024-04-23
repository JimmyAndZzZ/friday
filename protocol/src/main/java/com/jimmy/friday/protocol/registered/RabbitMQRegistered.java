package com.jimmy.friday.protocol.registered;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Sets;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.condition.RabbitMQCondition;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.Header;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.enums.SerializerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RegisteredType(type = ProtocolEnum.RABBITMQ, condition = RabbitMQCondition.class)
public class RabbitMQRegistered extends BaseRegistered {

    private Set<String> mqs = Sets.newConcurrentHashSet();

    private AmqpAdmin amqpAdmin;

    private RabbitTemplate rabbitTemplate;

    private ProtocolProperty protocolProperty;

    private ConnectionFactory connectionFactory;

    @Override
    public Set<String> getTopics() {
        Set<String> topics = Sets.newHashSet();
        String url = StrUtil.builder().append("http://").append(protocolProperty.getIp()).append(":15672/api/queues").toString();

        HttpRequest httpRequest = new HttpRequest(url);
        httpRequest.basicAuth(protocolProperty.getUsername(), protocolProperty.getPassword());
        HttpResponse execute = httpRequest.execute();
        String body = execute.body();
        if (StrUtil.isEmpty(body)) {
            return topics;
        }
        //获取所有队列名
        List<Map> list = JSONUtil.toList(body, Map.class);
        //添加动态监听
        for (Map map : list) {
            String queueName = MapUtil.getStr(map, "name");
            topics.add(queueName);
        }

        return topics;
    }

    @Override
    public void init(ProtocolProperty protocolProperty) {
        if (protocolProperty == null) {
            throw new IllegalArgumentException("未配置rabbitMQ参数");
        }

        CachingConnectionFactory connectionFactory = beanFactory.getBean(CachingConnectionFactory.class);
        connectionFactory.setHost(protocolProperty.getIp());
        connectionFactory.setPort(protocolProperty.getPort());
        connectionFactory.setUsername(protocolProperty.getUsername());
        connectionFactory.setPassword(protocolProperty.getPassword());
        connectionFactory.setVirtualHost("/");
        connectionFactory.setAddresses(protocolProperty.getIp() + ":" + protocolProperty.getPort());
        connectionFactory.setPublisherReturns(true);

        this.protocolProperty = protocolProperty;
        this.connectionFactory = connectionFactory;
        this.amqpAdmin = new RabbitAdmin(connectionFactory);
        this.rabbitTemplate = new RabbitTemplate(connectionFactory);
    }

    @Override
    public Output outputGene(Protocol info) {
        String mqQueneSender = info.getTopic();
        SerializerTypeEnum serializerType = info.getSerializerType();

        if (mqs.add(mqQueneSender)) {
            BeanDefinitionBuilder queueSender = BeanDefinitionBuilder.genericBeanDefinition(Queue.class);
            queueSender.addConstructorArgValue(mqQueneSender);
            queueSender.addConstructorArgValue(true);
            beanFactory.registerBeanDefinition(mqQueneSender + "Queue", queueSender.getRawBeanDefinition());
            Queue queue = (Queue) this.beanFactory.getBean(mqQueneSender + "Queue");
            amqpAdmin.declareQueue(queue);
        }

        return message -> {
            try {
                Map<String, Object> header = Header.getHeader();

                MessageProperties messageProperties = new MessageProperties();
                messageProperties.setTimestamp(new Date());
                messageProperties.setContentType("text/plain");
                messageProperties.setContentEncoding("UTF-8");
                messageProperties.setPriority(0);
                messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                //头信息处理
                if (MapUtil.isNotEmpty(header)) {
                    for (Map.Entry<String, Object> entry : header.entrySet()) {
                        messageProperties.setHeader(entry.getKey(), entry.getValue());
                    }
                }
                //序列化
                if (serializerType != null) {
                    message = serializerType.getSerializer().serialize(message);
                    messageProperties.setContentType(serializerType.getType());
                }

                rabbitTemplate.send(mqQueneSender, new Message(message.getBytes(StandardCharsets.UTF_8), messageProperties));
                return null;
            } finally {
                Header.clearHeader();
            }
        };
    }

    @Override
    public Input inputGene(Protocol info, Input input) {
        Boolean isAck = info.getIsAck();
        String mqQueneResver = info.getTopic();

        if (mqs.add(mqQueneResver)) {
            BeanDefinitionBuilder queueResver = BeanDefinitionBuilder.genericBeanDefinition(Queue.class);
            queueResver.addConstructorArgValue(mqQueneResver);
            queueResver.addConstructorArgValue(true);
            beanFactory.registerBeanDefinition(mqQueneResver + "Queue", queueResver.getRawBeanDefinition());
            Queue queue = (Queue) this.beanFactory.getBean(mqQueneResver + "Queue");
            amqpAdmin.declareQueue(queue);

            BeanDefinitionBuilder bd = BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class);
            bd.addPropertyValue("prefetchCount", info.getBatchSize());
            bd.addPropertyValue("connectionFactory", connectionFactory);
            bd.addPropertyValue("queueNames", new String[]{mqQueneResver});
            bd.addPropertyValue("messageListener", listener(isAck, input));
            bd.addPropertyValue("concurrentConsumers", info.getConcurrentConsumers());
            bd.addPropertyValue("maxConcurrentConsumers", info.getMaxConcurrentConsumers());
            bd.addPropertyValue("acknowledgeMode", isAck ? AcknowledgeMode.MANUAL : AcknowledgeMode.NONE);

            beanFactory.registerBeanDefinition(mqQueneResver + "Listener", bd.getRawBeanDefinition());
            SimpleMessageListenerContainer bean = (SimpleMessageListenerContainer) this.beanFactory.getBean(mqQueneResver + "Listener");
            bean.setAutoDeclare(false);
            bean.afterPropertiesSet();
            bean.start();
        }

        return input;
    }

    @Override
    public void close(Protocol info) {
        String mqQueneResver = info.getTopic();
        //删除监听器
        SimpleMessageListenerContainer bean = (SimpleMessageListenerContainer) this.beanFactory.getBean(mqQueneResver + "Listener");
        bean.stop();
        beanFactory.removeBeanDefinition(mqQueneResver + "Listener");
        //删除已注册信息
        mqs.remove(mqQueneResver);
        //删除队列
        beanFactory.removeBeanDefinition(mqQueneResver + "Queue");
    }

    /**
     * 注册
     *
     * @param isAck
     * @return
     */
    private ChannelAwareMessageListener listener(Boolean isAck, Input input) {
        return (message, channel) -> {
            MessageProperties messageProperties = message.getMessageProperties();
            try {
                byte[] body = message.getBody();
                String contentType = messageProperties.getContentType();
                Map<String, Object> headers = messageProperties.getHeaders();
                String messageBody = new String(body, messageProperties.getContentEncoding());
                //序列化判断
                if (StrUtil.isNotBlank(contentType)) {
                    SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.queryByType(contentType);
                    if (serializerTypeEnum != null) {
                        messageBody = serializerTypeEnum.getSerializer().deserialize(messageBody);
                    }
                }
                //头信息处理
                if (MapUtil.isNotEmpty(headers)) {
                    for (Map.Entry<String, Object> entry : headers.entrySet()) {
                        Header.putHeader(entry.getKey(), entry.getValue());
                    }
                }

                input.invoke(messageBody);

                if (isAck) {
                    channel.basicAck(messageProperties.getDeliveryTag(), false);
                }
            } catch (Throwable e) {
                log.error("Rabbitmq消费失败", e);

                if (isAck) {
                    channel.basicNack(messageProperties.getDeliveryTag(), false, true);
                }
            } finally {
                Header.clearHeader();
            }
        };
    }
}
