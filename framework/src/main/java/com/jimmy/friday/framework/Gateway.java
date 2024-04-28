package com.jimmy.friday.framework;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import com.jimmy.friday.boot.base.Callback;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.GrpcMethodEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.process.gateway.RpcProtocolInvokeProcess;
import com.jimmy.friday.framework.support.CallbackSupport;
import com.jimmy.friday.framework.utils.JsonUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class Gateway {

    private Gateway() {

    }

    public static GrpcBuilder grpc() {
        return new GrpcBuilder();
    }

    public static HttpBuilder http() {
        return new HttpBuilder();
    }

    public static Builder dubbo() {
        return new Builder(ServiceTypeEnum.DUBBO);
    }

    public static Builder gateway() {
        return new Builder(ServiceTypeEnum.GATEWAY);
    }

    public static SpringCloudBuilder springCloud() {
        return new SpringCloudBuilder();
    }

    public static class Builder {

        private Long traceId;

        private String appId;

        private Integer retry;

        private String methodId;

        private Integer timeout;

        private String serviceName;

        private byte[] attachment;

        private ServiceTypeEnum serviceType;

        private String version = GlobalConstants.DEFAULT_VERSION;

        private Map<String, String> tag = new HashMap<>();

        private Map<String, Object> params = new HashMap<>();

        public Builder(ServiceTypeEnum serviceType) {
            this.serviceType = serviceType;
            this.traceId = IdUtil.getSnowflake(1, 1).nextId();
        }

        public Builder setTimeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder setMethodId(String methodId) {
            this.methodId = methodId;
            return this;
        }

        public Builder setRetry(Integer retry) {
            this.retry = retry;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder addInvokeParam(String name, Object o) {
            this.params.put(name, o);
            return this;
        }

        public Builder addTag(String name, String value) {
            this.tag.put(name, value);
            return this;
        }

        public Builder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public GatewayResponse asyncExecute(Callback callback) {
            throw new GatewayException("not support");
        }

        public GatewayResponse execute() {
            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setId(this.traceId);
            gatewayRequest.setRetry(this.retry);
            gatewayRequest.setTimeout(this.timeout);
            gatewayRequest.setVersion(this.version);
            gatewayRequest.setServiceType(this.serviceType.toString());
            gatewayRequest.setMethodId(this.methodId);
            gatewayRequest.setServiceName(this.serviceName);
            gatewayRequest.setAppId(this.appId);
            gatewayRequest.setTag(this.tag);
            gatewayRequest.setAttachment(this.attachment);
            gatewayRequest.setClientName(Boot.getApplicationContext().getBean(ConfigLoad.class).getApplicationName());
            gatewayRequest.setApplicationId(Boot.getApplicationContext().getBean(ConfigLoad.class).getId());

            if (MapUtil.isNotEmpty(params)) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String name = entry.getKey();
                    Object value = entry.getValue();

                    InvokeParam invokeParam = new InvokeParam();
                    invokeParam.setName(name);

                    if (value != null) {
                        invokeParam.setClassName(value.getClass().getName());
                        invokeParam.setJsonData(JsonUtil.toString(value));
                    }

                    gatewayRequest.getInvokeParams().add(invokeParam);
                }
            }

            InetAddress firstNonLoopBackAddress = Boot.getApplicationContext().getBean(ConfigLoad.class).getLocalIpAddress();
            if (firstNonLoopBackAddress != null) {
                gatewayRequest.setClientIpAddress(firstNonLoopBackAddress.getHostAddress());
            }

            return Boot.getApplicationContext().getBean(RpcProtocolInvokeProcess.class).invoke(gatewayRequest);
        }
    }

    public static class GrpcBuilder extends Builder {

        private Integer timeout = GlobalConstants.DEFAULT_TIMEOUT;

        public GrpcBuilder() {
            super(ServiceTypeEnum.GRPC);
        }

        public GrpcBuilder setTimeout(Integer timeout) {
            super.setTimeout(timeout);
            this.timeout = timeout;
            return this;
        }

        public GrpcBuilder setRetry(Integer retry) {
            super.setRetry(retry);
            return this;
        }

        public GrpcBuilder setVersion(String version) {
            super.setVersion(version);
            return this;
        }

        public GrpcBuilder setServiceName(String serviceName) {
            super.setServiceName(serviceName);
            return this;
        }

        public GrpcBuilder addInvokeParam(String name, Object o) {
            super.addInvokeParam(name, o);
            return this;
        }

        public GrpcBuilder addTag(String name, String value) {
            super.addTag(name, value);
            return this;
        }

        public GrpcBuilder setAppId(String appId) {
            super.setAppId(appId);
            return this;
        }

        @Override
        public GatewayResponse execute() {
            super.setMethodId(GrpcMethodEnum.CALL.getMethodName());
            return super.execute();
        }

        @Override
        public GatewayResponse asyncExecute(Callback callback) {
            Boot.getApplicationContext().getBean(CallbackSupport.class).registerCallback(super.traceId, callback, timeout);
            super.setMethodId(GrpcMethodEnum.ASYNC_CALL.getMethodName());
            return super.execute();
        }
    }

    public static class HttpBuilder extends Builder {

        private File file;

        private Object body;

        public HttpBuilder() {
            super(ServiceTypeEnum.HTTP);
        }

        public HttpBuilder setTimeout(Integer timeout) {
            super.setTimeout(timeout);
            return this;
        }

        public HttpBuilder setMethodId(String methodId) {
            super.setMethodId(methodId);
            return this;
        }

        public HttpBuilder setRetry(Integer retry) {
            super.setRetry(retry);
            return this;
        }

        public HttpBuilder setVersion(String version) {
            super.setVersion(version);
            return this;
        }

        public HttpBuilder setServiceName(String serviceName) {
            super.setServiceName(serviceName);
            return this;
        }

        public HttpBuilder addInvokeParam(String name, Object o) {
            super.addInvokeParam(name, o);
            return this;
        }

        public HttpBuilder addTag(String name, String value) {
            super.addTag(name, value);
            return this;
        }

        public HttpBuilder setAppId(String appId) {
            super.setAppId(appId);
            return this;
        }

        public HttpBuilder setBody(Object body) {
            this.body = body;
            return this;
        }

        public HttpBuilder setFile(File file) {
            if (this.file != null) {
                throw new GatewayException("文件已存在");
            }

            if (file == null) {
                throw new GatewayException("文件为空");
            }

            this.file = file;
            return this;
        }

        @Override
        public GatewayResponse execute() {
            if (body != null) {
                this.addInvokeParam(AttributeConstants.Http.HTTP_BODY, body);
            }

            if (file != null) {
                this.addInvokeParam(ApiConstants.CONTEXT_PARAM_FILE_NAME, file.getName());

                try (FileChannel channel = new RandomAccessFile(file, "r").getChannel()) {
                    int fileSize = (int) channel.size();
                    MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize).load();
                    byte[] result = new byte[fileSize];
                    if (buffer.remaining() > 0) {
                        buffer.get(result, 0, fileSize);
                    }
                    buffer.clear();
                    super.attachment = result;
                } catch (Exception e) {
                    throw new GatewayException("文件读取失败");
                }
            }

            return super.execute();
        }
    }

    public static class SpringCloudBuilder extends Builder {

        private Object body;

        public SpringCloudBuilder() {
            super(ServiceTypeEnum.SPRING_CLOUD);
        }

        public SpringCloudBuilder setMethodId(String methodId) {
            super.setMethodId(methodId);
            return this;
        }

        public SpringCloudBuilder setRetry(Integer retry) {
            super.setRetry(retry);
            return this;
        }

        public SpringCloudBuilder setVersion(String version) {
            super.setVersion(version);
            return this;
        }

        public SpringCloudBuilder setServiceName(String serviceName) {
            super.setServiceName(serviceName);
            return this;
        }

        public SpringCloudBuilder addInvokeParam(String name, Object o) {
            super.addInvokeParam(name, o);
            return this;
        }

        public SpringCloudBuilder setAppId(String appId) {
            super.setAppId(appId);
            return this;
        }

        public SpringCloudBuilder setBody(Object body) {
            this.body = body;
            return this;
        }

        @Override
        public GatewayResponse execute() {
            this.addInvokeParam(AttributeConstants.Http.HTTP_BODY, body);
            return super.execute();
        }
    }
}
