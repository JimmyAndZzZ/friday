package com.jimmy.friday.center;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.*;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.exception.BusinessException;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.center.base.Hook;
import com.jimmy.friday.center.core.*;
import com.jimmy.friday.center.core.gateway.*;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.entity.GatewayInvokeTrace;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.event.InvokeEvent;
import com.jimmy.friday.center.exception.FallbackException;
import com.jimmy.friday.center.invoke.GatewayInvoke;
import com.jimmy.friday.center.service.*;
import com.jimmy.friday.center.support.InvokeSupport;
import com.jimmy.friday.center.support.LoadSupport;
import com.jimmy.friday.center.support.RegisterSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.utils.RedisConstants;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Gateway {

    @Autowired
    private RouteElect routeElect;

    @Autowired
    private LoadSupport loadSupport;

    @Autowired
    private GatewayInvoke gatewayInvoke;

    @Autowired
    private InvokeSupport invokeSupport;

    @Autowired
    private RegisterSupport registerSupport;

    @Autowired
    private CallbackManager callbackManager;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RateLimiterManager rateLimiterManager;

    @Autowired
    private CircuitBreakerManager circuitBreakerManager;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayInvokeTraceService gatewayInvokeTraceService;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Autowired
    private GatewayServiceProviderService gatewayServiceProviderService;

    public GatewayResponse run(GatewayRequest gatewayRequest) throws Throwable {
        String appId = gatewayRequest.getAppId();
        String version = gatewayRequest.getVersion();
        String clientName = gatewayRequest.getClientName();
        String serviceName = gatewayRequest.getServiceName();
        String serviceType = gatewayRequest.getServiceType();
        String clientIpAddress = gatewayRequest.getClientIpAddress();

        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.queryByType(serviceType);
        if (serviceTypeEnum == null) {
            throw new GatewayException("服务类型异常");
        }

        if (StrUtil.isEmpty(version)) {
            throw new GatewayException("版本号为空");
        }

        RegisterCenter center = registerSupport.get(serviceTypeEnum);
        if (center == null) {
            throw new GatewayException("服务类型异常");
        }

        List<Service> serviceList = center.getServiceList(serviceName, version);
        if (CollUtil.isEmpty(serviceList)) {
            throw new GatewayException("服务提供列表为空");
        }

        serviceList = routeElect.route(serviceList, gatewayRequest);

        if (CollUtil.isEmpty(serviceList)) {
            throw new GatewayException("服务提供列表为空");
        }

        Service load = loadSupport.load(serviceList, serviceTypeEnum.getLoadTypeEnum(), serviceTypeEnum, (service) -> true);
        if (load == null) {
            throw new GatewayException("当前服务全忙");
        }

        GatewayServiceProvider query = gatewayServiceProviderService.query(load);
        if (query == null) {
            throw new GatewayException("未查询到provider信息");
        }

        if (StrUtil.isEmpty(appId)) {
            throw new GatewayException("appId为空");
        }

        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            throw new GatewayException("账号不存在");
        }

        try {
            return this.invoke(serviceList, gatewayRequest, query, load, center);
        } finally {
            if (!gatewayRequest.getIsDebug() && !gatewayRequest.getIsApi()) {
                InvokeEvent invokeEvent = new InvokeEvent(applicationContext);
                invokeEvent.setAppId(appId);
                invokeEvent.setProviderId(query.getId());
                invokeEvent.setClientName(clientName);
                invokeEvent.setServiceId(query.getServiceId());
                invokeEvent.setIpAddress(clientIpAddress);
                applicationContext.publishEvent(invokeEvent);
            }
        }
    }

    /**
     * 调用服务
     *
     * @param gatewayRequest
     * @param provider
     * @param service
     * @param center
     * @return
     * @throws Exception
     */
    private GatewayResponse invoke(List<Service> serviceList, GatewayRequest gatewayRequest, GatewayServiceProvider provider, Service service, RegisterCenter center) throws Throwable {
        Long id = gatewayRequest.getId();
        String appId = gatewayRequest.getAppId();
        BigDecimal cost = gatewayRequest.getCost();
        String serviceType = gatewayRequest.getServiceType();
        String applicationId = gatewayRequest.getApplicationId();
        //加载参数
        ParamInfo paramInfo = this.loadParam(gatewayRequest);
        Map<String, String> arguments = paramInfo.getArguments();
        //获取调用方法
        Method method = this.getMethod(service, gatewayRequest, paramInfo.getParamClass());
        if (method == null) {
            throw new GatewayException("调用方法不存在");
        }

        GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.queryByMethod(method, gatewayServiceService.getGatewayService(service).getId());
        if (gatewayServiceMethod == null) {
            throw new GatewayException("调用方法不存在");
        }

        GatewayInvokeTrace gatewayInvokeTrace = new GatewayInvokeTrace();
        gatewayInvokeTrace.setId(id);
        gatewayInvokeTrace.setAppId(appId);
        gatewayInvokeTrace.setCreateTime(new Date());
        gatewayInvokeTrace.setProviderId(provider.getId());
        gatewayInvokeTrace.setServiceId(provider.getServiceId());
        gatewayInvokeTrace.setMethodId(gatewayServiceMethod.getId());
        gatewayInvokeTrace.setInvokeParam(JsonUtil.toString(arguments));
        gatewayInvokeTrace.setClientName(gatewayRequest.getClientName());
        gatewayInvokeTrace.setCost(cost != null ? cost : new BigDecimal(0));
        gatewayInvokeTrace.setClientIpAddress(gatewayRequest.getClientIpAddress());

        TimeInterval timeInterval = new TimeInterval();
        timeInterval.start();

        boolean isSync = true;
        try {
            GatewaySession.setTraceId(id);
            GatewaySession.setServiceId(service.getServiceId());

            if (!method.getIsSync()) {
                if (StrUtil.isEmpty(applicationId)) {
                    throw new GatewayException("没有回调接口，无法调用异步方法");
                }

                isSync = false;
                callbackManager.registerCallback(id, applicationId);
            }
            //勾子释放服务
            if (isSync) {
                GatewaySession.addHook(() -> center.releaseService(service.getServiceId()));
            }
            //服务限流key为服务名:服务类型:服务版本:方法code
            RateLimiter rateLimiter = rateLimiterManager.getRateLimiter(service.getName() + ":" + service.getType() + ":" + service.getVersion() + ":" + gatewayServiceMethod.getMethodCode());

            log.info("开始调用服务,id:{}", id);

            service.use();
            //断路器包装
            CheckedFunction0<GatewayResponse> circuitBreakerFunction = CircuitBreaker.decorateCheckedSupplier(circuitBreakerManager.getCircuitBreaker(service.getServiceId()), () -> {
                try {
                    return GatewayResponse.ok(invokeSupport.invoke(service, method, arguments));
                } catch (Exception e) {
                    if (e instanceof BusinessException) {
                        BusinessException businessException = (BusinessException) e;
                        return GatewayResponse.fail(businessException.getMessage(), businessException.getCode());
                    }

                    String fallbackMethodName = method.getFallbackMethod();

                    if (StrUtil.isEmpty(fallbackMethodName)) {
                        throw e;
                    }

                    try {
                        return GatewayResponse.ok(this.fallback(id, e, method, service, arguments));
                    } catch (Exception fallbackException) {
                        if (fallbackException instanceof BusinessException) {
                            BusinessException businessException = (BusinessException) fallbackException;
                            return GatewayResponse.fail(businessException.getMessage(), businessException.getCode());
                        }

                        log.error("熔断调用失败,id:{}", id, fallbackException);
                        throw e;
                    }
                }
            });
            //限流包装
            CheckedFunction0<GatewayResponse> rateLimiterFunction = RateLimiter.decorateCheckedSupplier(rateLimiter, circuitBreakerFunction);
            // 方法调用
            boolean tempIsSync = isSync;

            Try<GatewayResponse> submit = Try.of(rateLimiterFunction).recover(throwable -> {
                if (throwable instanceof RequestNotPermitted) {
                    log.info("当前请求受限，服务已限流,id:{}", id);
                    throw new GatewayException("当前请求受限，服务已限流");
                }

                if (throwable instanceof CallNotPermittedException) {
                    log.info("当前服务已被降级,id:{}", id);
                }
                //服务降级
                ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.queryByType(serviceType);
                Service load = loadSupport.load(serviceList, serviceTypeEnum.getLoadTypeEnum(), serviceTypeEnum, filter -> !filter.getServiceId().equals(service.getServiceId()));

                if (load == null) {
                    log.error("未加载到降级服务,id:{}", id);
                    throw new FallbackException(throwable);
                }

                log.info("当前服务降级开始调用,id:{}", id);

                load.use();

                try {
                    return GatewayResponse.ok(invokeSupport.invoke(load, method, arguments));
                } catch (Throwable t) {
                    //业务异常判断
                    if (t instanceof BusinessException) {
                        BusinessException businessException = (BusinessException) t;
                        return GatewayResponse.fail(businessException.getMessage(), businessException.getCode());
                    }

                    log.info("当前服务降级调用失败:{}", id);

                    throw new FallbackException(t);
                } finally {
                    if (tempIsSync) {
                        center.releaseService(load.getServiceId());
                    }
                }
            });
            //返回值处理
            GatewayResponse gatewayResponse = submit.get();
            //调用统计
            attachmentCache.increment(RedisConstants.Gateway.TODAY_INVOKE_COUNT + appId);
            attachmentCache.increment(RedisConstants.Gateway.GATEWAY_METHOD_TODAY_INVOKE_COUNT + gatewayServiceMethod.getId());
            attachmentCache.increment(RedisConstants.Gateway.GATEWAY_METHOD_HISTORY_INVOKE_COUNT + gatewayServiceMethod.getId());
            attachmentCache.attachString(RedisConstants.Gateway.GATEWAY_METHOD_LAST_INVOKE_TIME + gatewayServiceMethod.getId(), String.valueOf(System.currentTimeMillis()));
            attachmentCache.attachString(RedisConstants.Gateway.GATEWAY_SERVICE_LAST_INVOKE_TIME + gatewayServiceMethod.getServiceId(), String.valueOf(System.currentTimeMillis()));

            if (gatewayResponse.getIsSuccess()) {
                gatewayInvokeTrace.setIsSuccess(YesOrNoEnum.YES.getCode());
                gatewayInvokeTrace.setInvokeResult(StrUtil.subWithLength(gatewayResponse.getJsonResult(), 0, 2000));
                gatewayInvokeTrace.setCostTime(timeInterval.intervalMs());
            } else {
                gatewayInvokeTrace.setErrorMessage(gatewayResponse.getError());
                gatewayInvokeTrace.setIsSuccess(YesOrNoEnum.NO.getCode());
            }
            //保存调用信息
            this.gatewayInvokeTraceService.save(gatewayInvokeTrace);
            return gatewayResponse;
        } catch (Throwable e) {
            if (!isSync) {
                callbackManager.cancelCallback(id);
            }

            gatewayInvokeTrace.setErrorMessage(e.getMessage());
            gatewayInvokeTrace.setIsSuccess(YesOrNoEnum.NO.getCode());
            this.gatewayInvokeTraceService.save(gatewayInvokeTrace);
            //判断熔断异常
            throw e instanceof FallbackException ? ((FallbackException) e).getThrowable() : e;
        } finally {
            List<Hook> hooks = GatewaySession.getHooks();
            if (CollUtil.isNotEmpty(hooks)) {
                for (Hook hook : hooks) {
                    hook.hook();
                }
            }

            GatewaySession.clear();
        }
    }

    /**
     * 加载参数
     *
     * @param gatewayRequest
     * @return
     */
    private ParamInfo loadParam(GatewayRequest gatewayRequest) {
        Long id = gatewayRequest.getId();
        byte[] attachment = gatewayRequest.getAttachment();
        List<InvokeParam> invokeParams = gatewayRequest.getInvokeParams();

        ParamInfo paramInfo = new ParamInfo();

        if (CollUtil.isNotEmpty(invokeParams)) {
            for (InvokeParam invokeParam : invokeParams) {
                paramInfo.getParamClass().add(invokeParam.getClassName());
                paramInfo.getArguments().put(invokeParam.getName(), invokeParam.getJsonData());
            }
        }

        if (attachment != null) {
            String fileName = paramInfo.getArguments().get(ApiConstants.CONTEXT_PARAM_FILE_NAME);

            if (StrUtil.isNotEmpty(fileName)) {
                fileName = JsonUtil.parseObject(fileName, String.class);

                String filePath = "/tmp/" + id + "#" + fileName;
                FileUtil.writeBytes(attachment, filePath);

                paramInfo.getArguments().put(ApiConstants.CONTEXT_PARAM_FILE_NAME, fileName);
                paramInfo.getArguments().put(ApiConstants.CONTEXT_PARAM_FILE_PATH, filePath);

                GatewaySession.addHook(() -> FileUtil.del(filePath));
            }
        }

        return paramInfo;
    }

    /**
     * fallback调用
     *
     * @param exception
     * @param method
     * @param service
     * @param arguments
     * @return
     */
    private String fallback(Long id, Exception exception, Method method, Service service, Map<String, String> arguments) throws Exception {
        String fallbackClass = method.getFallbackClass();
        String fallbackMethodName = method.getFallbackMethod();
        Set<String> fallbackIgnoreExceptions = method.getFallbackIgnoreExceptions();

        if (StrUtil.isEmpty(fallbackMethodName)) {
            throw exception;
        }

        log.info("调用发生异常:{},进入熔断逻辑,id:{}", exception, id);

        if (CollUtil.isNotEmpty(fallbackIgnoreExceptions) && fallbackIgnoreExceptions.contains(exception.getClass().getName())) {
            log.info("当前熔断异常被忽略,id:{},exception:{}", id, exception.getClass().getName());

            throw new FallbackException(exception);
        }

        GatewaySession.setFallback(true);

        List<Param> invokeParams = Lists.newArrayList();
        invokeParams.addAll(method.getParams());
        invokeParams.addAll(method.getHttpPathParams());

        Method fallbackMethod = new Method();
        fallbackMethod.setIsSync(method.getIsSync());
        fallbackMethod.setInterfaceName(fallbackClass);
        fallbackMethod.setReturnType(method.getReturnType());
        fallbackMethod.setTimeout(method.getTimeout());
        fallbackMethod.setParams(invokeParams);
        fallbackMethod.setName(fallbackMethodName);
        return gatewayInvoke.invoke(service, fallbackMethod, arguments);
    }


    /**
     * 获取执行方法
     *
     * @param load
     * @param gatewayRequest
     * @param paramClass
     * @return
     */
    private Method getMethod(Service load, GatewayRequest gatewayRequest, List<String> paramClass) {
        Integer retry = gatewayRequest.getRetry();
        Integer timeout = gatewayRequest.getTimeout();
        String methodId = gatewayRequest.getMethodId();
        String methodCode = gatewayRequest.getMethodCode();
        String methodName = gatewayRequest.getMethodName();
        String invokeInterface = gatewayRequest.getInvokeInterface();

        Method method = this.findMethod(load, methodCode, methodId, invokeInterface, methodName, paramClass);
        return method == null ? null : ((timeout != null && timeout > 0) || (retry != null && retry > 0)) ? method.clone(timeout, retry) : method;
    }

    /**
     * 获取执行方法
     *
     * @param load
     * @param interfaceName
     * @param methodName
     * @param paramClass
     * @return
     */
    private Method findMethod(Service load, String methodCode, String methodId, String interfaceName, String methodName, List<String> paramClass) {
        List<Method> methods = load.getMethods();
        if (CollUtil.isEmpty(methods)) {
            return null;
        }

        if (StrUtil.isNotEmpty(methodId)) {
            List<Method> collect = methods.stream().filter(method -> method.getMethodId().equals(methodId)).collect(Collectors.toList());
            if (collect != null && !collect.isEmpty()) {
                return collect.stream().findFirst().get();
            }
        }

        if (StrUtil.isNotEmpty(methodCode)) {
            List<Method> collect = methods.stream().filter(method -> methodCode.equalsIgnoreCase(SecureUtil.md5(method.key()))).collect(Collectors.toList());
            if (collect != null && !collect.isEmpty()) {
                return collect.stream().findFirst().get();
            }
        }

        List<Method> collect = methods.stream().filter(method -> method.getName().equals(methodName) && method.getInterfaceName().equals(interfaceName)).collect(Collectors.toList());
        if (collect == null || collect.isEmpty()) {
            return null;
        }

        for (Method method : collect) {
            List<Param> params = method.getParams();

            if ((params == null || params.isEmpty()) && (paramClass == null || paramClass.isEmpty())) {
                return method;
            }

            if (params.size() == paramClass.size() && paramClass.equals(params.stream().map(Param::getType).collect(Collectors.toList()))) {
                return method;
            }
        }

        return null;
    }

    @Data
    private static class ParamInfo implements Serializable {

        private List<String> paramClass = Lists.newArrayList();

        private Map<String, String> arguments = Maps.newHashMap();
    }
}