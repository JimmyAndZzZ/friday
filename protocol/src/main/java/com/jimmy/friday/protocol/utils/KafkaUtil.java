package com.jimmy.friday.protocol.utils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class KafkaUtil {

    private KafkaUtil() {

    }

    /**
     * 获取kafka单个topic消息写入总量
     *
     * @param consumer
     * @param topic
     * @return
     */
    public static Long getEndOffset(String topic, KafkaConsumer<String, String> consumer) {
        // 获取分区信息
        List<PartitionInfo> partitionsFor = consumer.partitionsFor(topic);

        Collection<TopicPartition> partitions = new ArrayList<>();
        for (PartitionInfo partitionInfo : partitionsFor) {
            TopicPartition tp = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
            partitions.add(tp);
        }

        Map<TopicPartition, Long> endOffset = consumer.endOffsets(partitions);
        Long logSizeEndOffset = 0L;
        for (Map.Entry<TopicPartition, Long> topicPartition : endOffset.entrySet()) {
            logSizeEndOffset += topicPartition.getValue();
        }
        return logSizeEndOffset;
    }

    /**
     * 获取被提交的偏移量
     *
     * @param consumer
     * @return
     */
    public static Long getOffsetCommitted(String topic, KafkaConsumer<String, String> consumer) {
        Long commitedOffset = 0L;
        List<PartitionInfo> partitionsFor = consumer.partitionsFor(topic);
        for (PartitionInfo partitionInfo : partitionsFor) {
            TopicPartition tp = new TopicPartition(partitionInfo.topic(), partitionInfo.partition());
            OffsetAndMetadata committed = consumer.committed(tp);
            if (committed == null) {
                return 0L;
            }

            commitedOffset += committed.offset();
        }
        return commitedOffset;
    }
}
