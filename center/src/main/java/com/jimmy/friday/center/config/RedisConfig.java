package com.jimmy.friday.center.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config config = new Config();
        config.setCodec(new StringCodec());

        List<String> nodes = redisProperties.getCluster().getNodes();

        for (String node : nodes) {
            config.useClusterServers().addNodeAddress("redis://" + node);
        }

        return Redisson.create(config);
    }
}
