package com.jimmy.friday.protocol.registered;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.protocol.connector.KafkaConnector;
import com.jimmy.friday.protocol.exception.ProtocolException;
import com.jimmy.friday.protocol.annotations.RegisteredType;
import com.jimmy.friday.protocol.base.Input;
import com.jimmy.friday.protocol.base.Output;
import com.jimmy.friday.protocol.condition.KafkaCondition;
import com.jimmy.friday.protocol.config.ProtocolProperty;
import com.jimmy.friday.protocol.core.InputMessage;
import com.jimmy.friday.protocol.core.Protocol;
import com.jimmy.friday.protocol.enums.ProtocolEnum;
import com.jimmy.friday.protocol.enums.SerializerTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
@RegisteredType(type = ProtocolEnum.KAFKA, condition = KafkaCondition.class)
public class KafkaRegistered extends BaseRegistered {

    private final String HEADER_KEY_SERIALIZER = "serializer_type";

    private final Set<String> topics = Sets.newConcurrentHashSet();

    private final Map<KafkaConnector, Thread> connectors = Maps.newHashMap();

    private String kafkaServer;

    private volatile Producer producer;

    @Override
    public Set<String> getTopics() {
        Properties pro = new Properties();
        pro.put("bootstrap.servers", kafkaServer);
        try (AdminClient adminClient = KafkaAdminClient.create(pro)) {
            ListTopicsResult result = adminClient.listTopics();
            KafkaFuture<Set<String>> set = result.names();
            return set.get();
        } catch (Exception e) {
            throw new ProtocolException(e.getMessage());
        }
    }

    @Override
    public void init(ProtocolProperty protocolProperty) {
        if (protocolProperty == null) {
            throw new IllegalArgumentException("未配置kafka参数");
        }

        String ip = protocolProperty.getIp();
        List<String> split = StrUtil.split(ip, ",");
        this.kafkaServer = CollUtil.join(split.stream().map(str -> str + ":" + protocolProperty.getPort()).collect(Collectors.toList()), ",");
    }

    @Override
    public void close() {
        log.info("正在关闭kafka消费端");

        if (MapUtil.isNotEmpty(connectors)) {
            for (Map.Entry<KafkaConnector, Thread> entry : connectors.entrySet()) {
                entry.getKey().exit();
                try {
                    // 主线程继续执行，以便可以关闭consumer，提交偏移量
                    entry.getValue().join();
                } catch (InterruptedException e) {

                }
            }
        }

        log.info("成功关闭kafka消费端");
    }

    @Override
    public void close(Protocol info) {
        if (MapUtil.isNotEmpty(connectors)) {
            for (Map.Entry<KafkaConnector, Thread> entry : connectors.entrySet()) {
                KafkaConnector key = entry.getKey();

                if (key.getTopic().equalsIgnoreCase(info.getTopic()) && StrUtil.equalsIgnoreCase(key.getGroupId(), info.getGroupId())) {
                    key.exit();
                    try {
                        String str = inputKeyInit(info) + ":" + info.getGroupId();
                        topics.remove(str);
                        //删除spring容器
                        if (beanFactory.containsBeanDefinition(str + "Connector")) {
                            beanFactory.removeBeanDefinition(str + "Connector");
                        }
                        // 主线程继续执行，以便可以关闭consumer，提交偏移量
                        entry.getValue().join();
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
    }

    @Override
    public Output outputGene(Protocol info) {
        String mqQueneSender = info.getTopic();
        SerializerTypeEnum serializerType = info.getSerializerType();

        if (producer == null) {
            synchronized (KafkaRegistered.class) {
                if (producer == null) {
                    Properties props = new Properties();
                    //kafka服务器地址
                    props.put("bootstrap.servers", kafkaServer);
                    //ack是判断请求是否为完整的条件（即判断是否成功发送）。all将会阻塞消息，这种设置性能最低，但是最可靠。
                    props.put("acks", "1");
                    //retries,如果请求失败，生产者会自动重试，我们指定是0次，如果启用重试，则会有重复消息的可能性。
                    props.put("retries", 0);
                    //producer缓存每个分区未发送消息，缓存的大小是通过batch.size()配置设定的。值较大的话将会产生更大的批。并需要更多的内存(因为每个“活跃”的分区都有一个缓冲区)
                    props.put("batch.size", 16384);
                    //默认缓冲区可立即发送，即便缓冲区空间没有满；但是，如果你想减少请求的数量，可以设置linger.ms大于0.这将指示生产者发送请求之前等待一段时间
                    //希望更多的消息补填到未满的批中。这类似于tcp的算法，例如上面的代码段，可能100条消息在一个请求发送，因为我们设置了linger时间为1ms，然后，如果我们
                    //没有填满缓冲区，这个设置将增加1ms的延迟请求以等待更多的消息。需要注意的是，在高负载下，相近的时间一般也会组成批，即使是linger.ms=0。
                    //不处于高负载的情况下，如果设置比0大，以少量的延迟代价换取更少的，更有效的请求。
                    props.put("linger.ms", 1);
                    //buffer.memory控制生产者可用的缓存总量，如果消息发送速度比其传输到服务器的快，将会耗尽这个缓存空间。当缓存空间耗尽，其他发送调用将被阻塞，阻塞时间的阈值
                    //通过max.block.ms设定，之后他将抛出一个TimeoutExecption。
                    props.put("buffer.memory", 33554432);
                    //key.serializer和value.serializer示例：将用户提供的key和value对象ProducerRecord转换成字节，你可以使用附带的ByteArraySerizlizaer或StringSerializer处理简单的byte和String类型.
                    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                    //设置kafka的分区数量
                    props.put("kafka.partitions", 1);

                    producer = new KafkaProducer<>(props);
                }
            }
        }

        return message -> {
            try {
                List<Header> headers = Lists.newArrayList();
                //序列化
                if (serializerType != null) {
                    message = serializerType.getSerializer().serialize(message);

                    Header header = new RecordHeader(HEADER_KEY_SERIALIZER, serializerType.getType().getBytes(StandardCharsets.UTF_8));
                    headers.add(header);
                }
                Map<String, Object> header = com.jimmy.friday.protocol.core.Header.getHeader();
                //头信息处理
                if (MapUtil.isNotEmpty(header)) {
                    for (Map.Entry<String, Object> entry : header.entrySet()) {
                        Header h = new RecordHeader(entry.getKey(), entry.getValue().toString().getBytes(StandardCharsets.UTF_8));
                        headers.add(h);
                    }
                }

                ProducerRecord<String, String> record = new ProducerRecord<>(mqQueneSender, null, null, IdUtil.simpleUUID(), message, headers);
                Future<RecordMetadata> send = producer.send(record);
                producer.flush();
                return null;
            } catch (Exception e) {
                log.error("kafka发送失败", e);
                throw new ProtocolException(e.getMessage());
            } finally {
                com.jimmy.friday.protocol.core.Header.clearHeader();
            }
        };
    }

    @Override
    public Input inputGene(Protocol info, Input input) {
        String groupId = info.getGroupId();

        String key = inputKeyInit(info) + ":" + groupId;
        if (topics.add(key)) {
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(KafkaConnector.class);
            beanDefinitionBuilder.addConstructorArgValue(kafkaServer);
            beanDefinitionBuilder.addConstructorArgValue(info.getTopic());
            beanDefinitionBuilder.addConstructorArgValue(null);
            beanDefinitionBuilder.addConstructorArgValue(info.getGroupId());
            beanDefinitionBuilder.addConstructorArgValue(info.getBatchSize());
            // 注册bean
            beanFactory.registerBeanDefinition(key + "Connector", beanDefinitionBuilder.getRawBeanDefinition());
            KafkaConnector bean = (KafkaConnector) this.beanFactory.getBean(key + "Connector");

            Thread thread = new Thread(() -> process(bean, input, info.getIsAck()));
            this.connectors.put(bean, thread);
            thread.start();
        }

        return input;
    }

    /**
     * 处理Kafka消息
     *
     * @param connector
     * @param input
     */
    private void process(KafkaConnector connector, Input input, Boolean isAck) {
        try {
            connector.connect();
            connector.subscribe();
            // 消息起始偏移地址
            long offset = -1;
            while (true) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    List<InputMessage> messages = connector.getFlatListWithoutAck(1000L, offset); // 获取message
                    if (CollUtil.isNotEmpty(messages)) {
                        for (InputMessage message : messages) {
                            // 记录第一条消息的offset，用于处理数据异常时重新从此位置获取消息
                            if (offset < 0) {
                                offset = message.getOffset();
                            }

                            this.messageConsume(message, input);
                        }

                        connector.ack();
                        // 还原offset
                        offset = -1;
                    }

                } catch (Throwable e) {
                    if (e instanceof WakeupException) {
                        throw e;
                    } else {
                        log.error("Kafka处理消息失败", e);
                    }
                    if (!isAck) {
                        // 还原offset
                        offset = -1;
                    }
                }
            }
        } catch (Throwable e) {
            log.error("Kafka断开连接", e);
        }

        connector.unsubscribe();
        connector.close();
    }

    /**
     * 消费消息
     *
     * @param message
     * @param input
     * @throws Exception
     */
    private void messageConsume(InputMessage message, Input input) throws Exception {
        try {
            String msg = message.getMessage();
            Headers headers = message.getHeaders();

            if (CollUtil.isNotEmpty(headers)) {
                for (Header header : headers) {
                    String key = header.key();
                    String value = new String(header.value(), StandardCharsets.UTF_8);

                    if (HEADER_KEY_SERIALIZER.equalsIgnoreCase(key)) {
                        SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.queryByType(value);
                        if (serializerTypeEnum != null) {
                            msg = serializerTypeEnum.getSerializer().deserialize(msg);
                        }

                        continue;
                    }

                    com.jimmy.friday.protocol.core.Header.putHeader(key, value);
                }
            }


            input.invoke(msg);
        } finally {
            com.jimmy.friday.protocol.core.Header.clearHeader();
        }
    }
}
