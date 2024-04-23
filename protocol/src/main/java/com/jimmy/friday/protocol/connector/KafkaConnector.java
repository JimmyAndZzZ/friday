package com.jimmy.friday.protocol.connector;

import com.google.common.collect.Lists;
import com.jimmy.friday.protocol.core.InputMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KafkaConnector {
    protected KafkaConsumer<String, String> kafkaConsumer;
    // 用于扁平message的数据消费
    private String topic;

    private Integer partition;

    private Properties properties;

    private String servers;

    private String groupId;

    private volatile boolean connected = false;
    private volatile boolean running = false;

    private Map<Integer, Long> currentOffsets = new ConcurrentHashMap<>();

    public KafkaConnector(String servers, String topic, Integer partition, String groupId, Integer batchSize) {
        this.topic = topic;
        this.partition = partition;
        this.servers = servers;
        this.groupId = groupId;

        properties = new Properties();
        properties.put("bootstrap.servers", servers);
        properties.put("group.id", groupId);
        properties.put("enable.auto.commit", false);
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "latest"); // 如果没有offset则从最后的offset开始读
        properties.put("session.timeout.ms", "6000"); // 默认为30秒
        properties.put("max.poll.interval.ms", "500000");
        properties.put("heartbeat.interval.ms", "2000");
        properties.put("isolation.level", "read_committed");
        properties.put("max.poll.records", batchSize.toString());
        properties.put("key.deserializer", StringDeserializer.class.getName());
        properties.put("value.deserializer", StringDeserializer.class.getName());
    }

    /**
     * 打开连接
     */
    public void connect() {
        if (connected) {
            return;
        }

        connected = true;
        kafkaConsumer = new KafkaConsumer<>(properties);
    }

    public String getGroupId() {
        return groupId;
    }

    protected void waitClientRunning() {
        running = true;
    }

    /**
     * 订阅topic
     */
    public void subscribe() {
        waitClientRunning();
        if (!running) {
            return;
        }

        if (partition == null) {
            if (kafkaConsumer != null) {
                kafkaConsumer.subscribe(Collections.singletonList(topic));
            }
        } else {
            TopicPartition topicPartition = new TopicPartition(topic, partition);
            if (kafkaConsumer != null) {
                kafkaConsumer.assign(Collections.singletonList(topicPartition));
            }
        }
    }

    /**
     * 取消订阅
     */
    public void unsubscribe() {
        log.info("kafka断开连接");

        waitClientRunning();
        if (!running) {
            return;
        }

        if (kafkaConsumer != null) {
            kafkaConsumer.unsubscribe();
        }
    }

    public List<InputMessage> getFlatListWithoutAck(Long timeout, long offset) {
        waitClientRunning();
        if (!running) {
            return Lists.newArrayList();
        }

        if (offset > -1) {
            TopicPartition tp = new TopicPartition(topic, partition == null ? 0 : partition);
            kafkaConsumer.seek(tp, offset);
        }

        ConsumerRecords<String, String> records = kafkaConsumer.poll(timeout);

        if (records.isEmpty()) {
            Thread.yield();
            return Lists.newArrayList();
        }

        if (!records.isEmpty()) {
            List<InputMessage> message = Lists.newArrayList();
            for (ConsumerRecord<String, String> record : records) {
                InputMessage m = new InputMessage();
                m.setMessage(record.value());
                m.setOffset(record.offset());
                m.setHeaders(record.headers());
                message.add(m);
            }

            return message;
        }

        return Lists.newArrayList();
    }

    public String getNameServer() {
        return this.servers;
    }

    public String getTopic() {
        return this.topic;
    }

    public void rollback() {
        waitClientRunning();
        if (!running) {
            return;
        }
        // 回滚所有分区
        if (kafkaConsumer != null) {
            for (Map.Entry<Integer, Long> entry : currentOffsets.entrySet()) {
                kafkaConsumer.seek(new TopicPartition(topic, entry.getKey()), entry.getValue() - 1);
            }
        }
    }

    /**
     * 提交offset，如果超过 session.timeout.ms 设置的时间没有ack则会抛出异常，ack失败
     */
    public void ack() {
        waitClientRunning();
        if (!running) {
            return;
        }

        if (kafkaConsumer != null) {
            kafkaConsumer.commitSync();
        }
    }

    public void exit() {
        kafkaConsumer.wakeup();
    }

    public void close() {
        kafkaConsumer.close();
    }
}
