package com.jimmy.friday.boot.other;

public interface ConfigConstants {

    String TRANSACTION_POINT_ROOT_PATH = "TRANSACTION_POINT_ROOT_PATH";

    String HEADER_TRACE_ID_KEY = "x-agent-trace-id";

    String HEADER_LOG_NEED_PUSH_KEY = "x-agent-log-need-push";

    String HEADER_PRODUCER_KEY = "x-agent-producer";

    String CONTEXT_ATTR_NAME = "_$EnhancedClassField_ws";

    String APPLICATION_NAME = "APPLICATION_NAME";

    String GROUP_NAME = "GROUP_NAME";

    String COLLECTOR_PATH = "COLLECTOR_PATH";

    String LOG_PUSH_LEVEL = "LOG_LEVEL";

    String LOG_ALL_PUSH = "LOG_ALL_PUSH";

    String LOG_COLLECTOR_POINT = "LOG_COLLECTOR_POINT.";

    String QPS_COLLECTOR_POINT = "QPS_COLLECTOR_POINT.";

    String IGNORE_COLLECTOR_PATH = "IGNORE_COLLECTOR_PATH";

    String IGNORE_COLLECTOR_CLASS = "IGNORE_COLLECTOR_CLASS";

    String MATCH_HTTP_URL = "MATCH_HTTP_URL";

    String HTTP_ALL_PUSH = "HTTP_ALL_PUSH";

    String QPS_ALL_PUSH = "QPS_ALL_PUSH";

    String ADDRESS = "ADDRESS";

    String BATCH_SIZE = "BATCH_SIZE";

    String COLLECTOR_SERVER = "COLLECTOR_SERVER";

    String ID = "ID";

    String VERSION = "VERSION";

    String APP_ID = "APP_ID";

    String WEIGHT = "WEIGHT";

    String OFFSET_PATH = "OFFSET_PATH";

    String AGENT_ACTION_PATH = "com.jimmy.friday.agent.plugin.action";

    String IGNORED_NETWORK_INTERFACES = "IGNORED_NETWORK_INTERFACES";

    String PREFERRED_NETWORKS = "PREFERRED_NETWORKS";
}
