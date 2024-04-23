package com.jimmy.friday.center.utils;

public interface RedisConstants {

    String TRANSACTION_FACTS = "ss:gateway:transaction:facts:";

    String TRANSACTION_POINT = "ss:gateway:transaction:point:";

    String HEARTBEAT_FAIL_COUNT = "ss:gateway:heartbeat:fail:count:";

    String SERVICE_USE_STATUS = "ss:gateway:service:use:status:";

    String SERVICE_REGISTER_FLAG = "ss:gateway:service:register:flag:";

    String API_INVOKE_COUNT = "ss:gateway:api:invoke:count:";

    String METHOD_OPEN_CACHE = "ss:gateway:method:open:cache";

    String SERVICE_METHOD_CACHE = "ss:gateway:service:method:cache";

    String SERVICE_PROVIDER_CACHE = "ss:gateway:service:provider:cache";

    String GATEWAY_SERVICE_CACHE = "ss:gateway:service:cache";

    String GATEWAY_SERVICE_ID_MAPPER = "ss:gateway:service:id:mapper";

    String SERVICE_METHOD_ID_MAPPER = "ss:gateway:service:method:id:mapper";

    String ROUTE_RULE_CACHE = "ss:gateway:route:rule";

    String GATEWAY_ACCOUNT_CACHE = "ss:gateway:account:cache:";

    String GATEWAY_ACCOUNT_APP_ID_CACHE = "ss:gateway:account:app:id:cache";

    String COST_STRATEGY_CACHE = "ss:gateway:account:cache";

    String TODAY_COST_AMOUNT = "ss:gateway:today:cost:amount:";

    String TODAY_INVOKE_COUNT = "ss:gateway:today:invoke:count:";

    String COST_STRATEGY_DETAILS_CACHE = "ss:gateway:account:details:cache:";

    String GATEWAY_SERVICE_STATUS_REFRESH_JOB = "ss:gateway:service:status:refresh:job";

    String GATEWAY_INVOKE_CALLBACK = "ss:gateway:invoke:callback:";

    String TRANSACTION_READ_WRITE_LOCK = "ss:gateway:transaction:read:write:lock:";

    String GATEWAY_CHANNEL_SUB = "ss:gateway:channel:sub:";

    String GATEWAY_CHANNEL_CACHE = "ss:gateway:channel:cache:";

    String GATEWAY_CHANNEL_SUB_SAVE_LOCK = "ss:gateway:channel:sub:save:lock:";

    String GATEWAY_CHANNEL_CURRENT_OFFSET = "ss:gateway:channel:current:offset:";

    String GATEWAY_METHOD_TODAY_INVOKE_COUNT = "ss:gateway:method:today:invoke:count:";

    String GATEWAY_METHOD_HISTORY_INVOKE_COUNT = "ss:gateway:method:history:invoke:count:";

    String GATEWAY_METHOD_LAST_INVOKE_TIME = "ss:gateway:method:last:invoke:time:";

    String GATEWAY_SERVICE_CONSUMER = "ss:gateway:service:consumer:";
}
