package com.jimmy.friday.center.utils;

public interface RedisConstants {

    interface Transaction {
        String TRANSACTION_FACTS = "friday:gateway:transaction:facts:";

        String TRANSACTION_POINT = "friday:gateway:transaction:point:";

        String TRANSACTION_READ_WRITE_LOCK = "friday:gateway:transaction:read:write:lock:";
    }

    interface Gateway {
        String HEARTBEAT_FAIL_COUNT = "friday:gateway:heartbeat:fail:count:";

        String SERVICE_USE_STATUS = "friday:gateway:service:use:status:";

        String SERVICE_REGISTER_FLAG = "friday:gateway:service:register:flag:";

        String API_INVOKE_COUNT = "friday:gateway:api:invoke:count:";

        String METHOD_OPEN_CACHE = "friday:gateway:method:open:cache";

        String SERVICE_METHOD_CACHE = "friday:gateway:service:method:cache";

        String SERVICE_PROVIDER_CACHE = "friday:gateway:service:provider:cache";

        String GATEWAY_SERVICE_CACHE = "friday:gateway:service:cache";

        String GATEWAY_SERVICE_ID_MAPPER = "friday:gateway:service:id:mapper";

        String SERVICE_METHOD_ID_MAPPER = "friday:gateway:service:method:id:mapper";

        String ROUTE_RULE_CACHE = "friday:gateway:route:rule";

        String GATEWAY_ACCOUNT_CACHE = "friday:gateway:account:cache:";

        String GATEWAY_ACCOUNT_APP_ID_CACHE = "friday:gateway:account:app:id:cache";

        String COST_STRATEGY_CACHE = "friday:gateway:account:cache";

        String TODAY_COST_AMOUNT = "friday:gateway:today:cost:amount:";

        String TODAY_INVOKE_COUNT = "friday:gateway:today:invoke:count:";

        String COST_STRATEGY_DETAILS_CACHE = "friday:gateway:account:details:cache:";

        String GATEWAY_INVOKE_CALLBACK = "friday:gateway:invoke:callback:";

        String GATEWAY_CHANNEL_SUB = "friday:gateway:channel:sub:";

        String GATEWAY_CHANNEL_CACHE = "friday:gateway:channel:cache:";

        String GATEWAY_CHANNEL_SUB_SAVE_LOCK = "friday:gateway:channel:sub:save:lock:";

        String GATEWAY_CHANNEL_CURRENT_OFFSET = "friday:gateway:channel:current:offset:";

        String GATEWAY_METHOD_TODAY_INVOKE_COUNT = "friday:gateway:method:today:invoke:count:";

        String GATEWAY_METHOD_HISTORY_INVOKE_COUNT = "friday:gateway:method:history:invoke:count:";

        String GATEWAY_SERVICE_LAST_INVOKE_TIME = "friday:gateway:service:last:invoke:time:";

        String GATEWAY_METHOD_LAST_INVOKE_TIME = "friday:gateway:method:last:invoke:time:";

        String GATEWAY_SERVICE_CONSUMER = "friday:gateway:service:consumer:";
    }


}
