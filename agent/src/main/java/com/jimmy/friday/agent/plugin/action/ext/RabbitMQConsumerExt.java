package com.jimmy.friday.agent.plugin.action.ext;

import com.jimmy.friday.boot.core.agent.Topology;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.support.RunTopologySupport;
import com.jimmy.friday.agent.support.TopologySupport;
import com.jimmy.friday.boot.other.ConfigConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class RabbitMQConsumerExt implements Consumer {

    private String serverUrl;

    private Consumer delegate;

    public RabbitMQConsumerExt(final Consumer delegate, final String serverUrl) {
        this.delegate = delegate;
        this.serverUrl = serverUrl;
    }

    @Override
    public void handleConsumeOk(final String consumerTag) {
        this.delegate.handleConsumeOk(consumerTag);
    }

    @Override
    public void handleCancelOk(final String consumerTag) {
        this.delegate.handleRecoverOk(consumerTag);
    }

    @Override
    public void handleCancel(final String consumerTag) throws IOException {
        this.delegate.handleCancel(consumerTag);
    }

    @Override
    public void handleShutdownSignal(final String consumerTag, final ShutdownSignalException sig) {
        this.delegate.handleShutdownSignal(consumerTag, sig);
    }

    @Override
    public void handleRecoverOk(final String consumerTag) {
        this.delegate.handleRecoverOk(consumerTag);
    }

    @Override
    public void handleDelivery(final String consumerTag,
                               final Envelope envelope,
                               final AMQP.BasicProperties properties,
                               final byte[] body) throws IOException {

        Map<String, Object> headers = properties.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            String invokeRemark = "/Queue:" + envelope.getRoutingKey();

            Object o = headers.get(ConfigConstants.HEADER_TRACE_ID_KEY);

            String[] split = this.serverUrl.split(",");
            for (String s : split) {
                Topology rabbitmq = new Topology();
                rabbitmq.setMachine(s);
                rabbitmq.setApplication("rabbitmq");
                rabbitmq.setType("rabbitmq");
                TopologySupport.getInstance().push(rabbitmq, ConfigLoad.getDefault().getTopology(), invokeRemark, "rabbitmq");

                if (o != null) {
                    Object logNeedPush = headers.get(ConfigConstants.HEADER_LOG_NEED_PUSH_KEY);
                    if (logNeedPush != null && Boolean.valueOf(logNeedPush.toString())) {
                        RunTopologySupport.getInstance().send(rabbitmq, ConfigLoad.getDefault().getTopology(), invokeRemark, "rabbitmq", o.toString());
                    }
                }
            }
        }

        this.delegate.handleDelivery(consumerTag, envelope, properties, body);
    }
}
