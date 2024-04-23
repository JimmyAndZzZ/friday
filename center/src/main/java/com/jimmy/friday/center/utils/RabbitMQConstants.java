package com.jimmy.friday.center.utils;

public interface RabbitMQConstants {

    String DELAYED_QUEUE_NAME = "ss_gateway_invoke_delayed_queue";

    String DLX_EXCHANGE_NAME = "ss_gateway_invoke_dlx_exchange";

    String DLX_QUEUE_NAME = "ss_gateway_invoke_dlx_queue";

    String DLX_ROUTE_KEY = "ss_gateway_invoke_dlx_routing_key";
}
