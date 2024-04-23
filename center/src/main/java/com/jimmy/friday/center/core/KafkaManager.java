package com.jimmy.friday.center.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.center.base.Close;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.base.Process;
import com.jimmy.friday.center.config.GatewayConfigProperties;
import com.jimmy.friday.protocol.connector.KafkaConnector;
import com.jimmy.friday.protocol.core.InputMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
@Component
public class KafkaManager implements Initialize, Close {

    private final Set<String> topics = Sets.newConcurrentHashSet();

    private final Map<KafkaConnector, Thread> connectors = Maps.newHashMap();

    private Producer<String, String> producer;

    @Autowired
    private GatewayConfigProperties gatewayConfigProperties;

    public void send(String topic, String message) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, null, IdUtil.simpleUUID(), message);
        producer.send(record);
        producer.flush();
    }

    public void close(String topic, String groupId) {
        if (MapUtil.isNotEmpty(connectors)) {
            for (Map.Entry<KafkaConnector, Thread> entry : connectors.entrySet()) {
                KafkaConnector key = entry.getKey();

                if (key.getTopic().equalsIgnoreCase(topic) && StrUtil.equalsIgnoreCase(key.getGroupId(), groupId)) {
                    topics.remove(topic);
                    key.exit();
                    try {
                        // 主线程继续执行，以便可以关闭consumer，提交偏移量
                        entry.getValue().join();
                    } catch (InterruptedException e) {

                    }
                }
            }
        }
    }

    public void receive(String topic, String groupId, Integer batchSize, Process process) {
        if (topics.add(topic)) {
            KafkaConnector kafkaConnector = new KafkaConnector(gatewayConfigProperties.getKafkaServer(), topic, null, groupId, batchSize);
            Thread thread = new Thread(() -> process(kafkaConnector, process, true));
            this.connectors.put(kafkaConnector, thread);
            thread.start();
        }
    }

    @Override
    public void init() throws Exception {
        String kafkaServer = gatewayConfigProperties.getKafkaServer();
        if (StrUtil.isEmpty(kafkaServer)) {
            throw new IllegalArgumentException("未配置kafka地址");
        }

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

    @Override
    public int sort() {
        return 0;
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

    /**
     * 处理Kafka消息
     *
     * @param connector
     * @param process
     * @param isAck
     */
    private void process(KafkaConnector connector, Process process, Boolean isAck) {
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

                            process.process(message);
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
}
