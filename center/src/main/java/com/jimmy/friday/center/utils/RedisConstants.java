package com.jimmy.friday.center.utils;

public interface RedisConstants {

    interface Transaction {

        String TRANSACTION_FACTS = "friday:transaction:facts:";

        String TRANSACTION_POINT = "friday:transaction:point:";

        String TRANSACTION_READ_WRITE_LOCK = "friday:transaction:read:write:lock:";

        String TRANSACTION_TIMEOUT_JOB_LOCK = "friday:transaction:timeout:job:lock";
    }

    interface Schedule {

        String SCHEDULE_JOB_CACHE = "friday:schedule:jon:cache";

        String SCHEDULE_EXECUTE_COUNT = "friday:schedule:execute:count:";

        String SCHEDULE_EXECUTE_FAIL_COUNT = "friday:schedule:execute:fail:count:";

        String SCHEDULE_EXECUTE_JOB_LOCK = "friday:schedule:execute:job:lock:";

        String SCHEDULE_EXECUTOR_REGISTER = "friday:schedule:execute:register:";

        String SCHEDULE_EXECUTOR_CACHE = "friday:schedule:execute:cache";

        String SCHEDULE_EXECUTOR_APPLICATION_WEIGHT = "friday:schedule:executor:application:weight";

        String SCHEDULE_EXECUTOR_LAST_INVOKE_DATE = "friday:schedule:executor:last:invoke:date";

        String SCHEDULE_REGISTER_JOB_LOCK = "friday:schedule:register:job:lock:";

        String SCHEDULE_JOB_RUNNING_FLAG = "friday:schedule:job:running:flag:";

        String SCHEDULE_TIMEOUT_JOB_LOCK = "friday:schedule:timeout:job:lock";

        String SCHEDULE_JOB_RELOAD_LOCK = "friday:schedule:job:reload:lock:";

        String SCHEDULE_NO_TIMEOUT_JOB_SCAN_LOCK = "friday:schedule:no:timeout:job:scan:lock";

        String SCHEDULE_JOB_CODE_ID_MAPPER = "friday:schedule:job:code:id:mapper:";

        String SCHEDULE_JOB_RUNNING_SHARDING_NUM = "friday:schedule:job:running:sharding:num:";
    }

    interface Gateway {

        String GATEWAY_ACCOUNT_INVOKE_COUNT_JOB_LOCK = "friday:gateway:account:invoke:job:lock";

        String GATEWAY_INVOKE_METRICS_JOB_LOCK = "friday:gateway:invoke:metrics:job:lock";

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
