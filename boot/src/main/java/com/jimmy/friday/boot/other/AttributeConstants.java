package com.jimmy.friday.boot.other;

public interface AttributeConstants {

    interface Dubbo {
        String REGISTRY_CONFIG_ADDRESS = "DUBBO_REGISTRY_CONFIG_ADDRESS";

        String PROVIDER_PROTOCOL_TYPE = "DUBBO_PROVIDER_PROTOCOL_TYPE";
    }

    interface Http {
        String HTTP_BODY = "HTTP_BODY";

        String SERVER_SERVLET_CONTEXT_PATH = "SERVER_SERVLET_CONTEXT_PATH";

        String HEADER_EXCEPTION_CLASS = "HEADER_EXCEPTION_CLASS";

        String HEADER_EXCEPTION_MESSAGE = "HEADER_EXCEPTION_MESSAGE";

        String HTTP_MULTIPART_FILE_PARAM_NAME = "file";
    }
}
