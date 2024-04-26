package com.jimmy.friday.center.core.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jimmy.friday.boot.core.gateway.Method;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.ServiceStatusEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.boot.enums.ServiceWarnTypeEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.base.Invoke;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceMethod;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.event.ServiceWarnEvent;
import com.jimmy.friday.center.service.GatewayServiceMethodParamService;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.service.GatewayServiceProviderService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.utils.RedisConstants;
import com.jimmy.friday.center.vo.gateway.AdjustMethodArgumentVO;
import com.jimmy.friday.center.vo.gateway.AdjustServiceArgumentVO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Slf4j
public class RegisterCenter {

    private final Set<String> suspected = Sets.newHashSet();

    private final Map<String, Service> serviceMap = Maps.newHashMap();

    private final Map<String, Set<String>> serviceVersion = Maps.newHashMap();

    @Getter
    private final ConcurrentMap<String, List<Service>> serviceList = Maps.newConcurrentMap();

    private final String id;

    private final Invoke invoke;

    private final AtomicBoolean process;

    private final String stripedLockName;

    private final StripedLock stripedLock;

    private final AttachmentCache attachmentCache;

    private final ScheduledExecutorService executor;

    private final ApplicationContext applicationContext;

    private final GatewayServiceService gatewayServiceService;

    private final GatewayCircuitBreakerManager gatewayCircuitBreakerManager;

    private final GatewayServiceMethodService gatewayServiceMethodService;

    private final GatewayServiceProviderService gatewayServiceProviderService;

    private final GatewayServiceMethodParamService gatewayServiceMethodParamService;

    public RegisterCenter(ApplicationContext applicationContext, ServiceTypeEnum serviceType, StripedLock stripedLock, Invoke invoke, AttachmentCache attachmentCache, GatewayServiceProviderService gatewayServiceProviderService, GatewayServiceMethodService gatewayServiceMethodService, GatewayCircuitBreakerManager gatewayCircuitBreakerManager, GatewayServiceService gatewayServiceService, GatewayServiceMethodParamService gatewayServiceMethodParamService) {
        this.invoke = invoke;
        this.id = IdUtil.simpleUUID();
        this.stripedLock = stripedLock;
        this.attachmentCache = attachmentCache;
        this.applicationContext = applicationContext;
        this.process = new AtomicBoolean(false);
        this.gatewayCircuitBreakerManager = gatewayCircuitBreakerManager;
        this.gatewayServiceService = gatewayServiceService;
        this.stripedLockName = serviceType.toString() + "Register";
        this.executor = Executors.newScheduledThreadPool(1);
        this.gatewayServiceMethodService = gatewayServiceMethodService;
        this.gatewayServiceProviderService = gatewayServiceProviderService;
        this.gatewayServiceMethodParamService = gatewayServiceMethodParamService;

        this.executor.scheduleAtFixedRate(() -> {
            if (MapUtil.isEmpty(serviceMap)) {
                return;
            }

            if (!process.compareAndSet(false, true)) {
                return;
            }

            try {
                for (Map.Entry<String, Service> entry : serviceMap.entrySet()) {
                    String name = entry.getKey();
                    Service service = entry.getValue();

                    String redisKey = this.getServiceRedisKey(RedisConstants.Gateway.HEARTBEAT_FAIL_COUNT, service);

                    if (this.invoke.heartbeat(service)) {
                        log.debug("收到心跳响应:{},地址:{},端口:{}", name, service.getIpAddress(), service.getPort());

                        this.suspected.remove(service.getApplicationId());
                        this.attachmentCache.remove(redisKey);

                        ServiceStatusEnum status = service.getStatus();
                        if (!status.equals(ServiceStatusEnum.ALIVE)) {
                            service.setStatus(ServiceStatusEnum.ALIVE);
                            gatewayServiceProviderService.update(service, false);
                        }

                        continue;
                    }

                    this.suspected.add(service.getApplicationId());

                    Long increment = this.attachmentCache.increment(redisKey);
                    this.attachmentCache.expire(redisKey, 2L, TimeUnit.HOURS);
                    if (increment > 5) {
                        this.remove(service, false);
                    } else if (increment > 0 && increment < 3) {
                        log.info("服务名:{},地址:{},端口:{},服务疑似断开连接", name, service.getIpAddress(), service.getPort());
                        service.setStatus(ServiceStatusEnum.ABNORMAL);
                        gatewayServiceProviderService.update(service, false);

                        this.warn(ServiceWarnTypeEnum.HEARTBEAT_ERROR, service);
                    } else {
                        log.info("服务名:{},地址:{},端口:{},服务心跳异常", name, service.getIpAddress(), service.getPort());
                        service.setStatus(ServiceStatusEnum.DISCONNECT);
                        gatewayServiceProviderService.update(service, false);

                        this.warn(ServiceWarnTypeEnum.HEARTBEAT_ERROR, service);
                    }
                }
            } finally {
                this.process.set(false);
            }
        }, 0L, 10, TimeUnit.SECONDS);
    }

    public void close() {
        executor.shutdownNow();
        serviceMap.clear();
        serviceList.clear();
    }

    public Service getByServiceId(String key) {
        return this.serviceMap.get(key);
    }

    public void remove(Service remove, boolean isForce) {
        String name = remove.getName();
        String serviceId = this.getServiceId(remove);

        Lock lock = stripedLock.getLocalLock(stripedLockName, 16, serviceId);

        lock.lock();
        try {
            Service service = serviceMap.get(serviceId);
            if (service == null) {
                return;
            }

            if (!isForce && !service.getStatus().equals(ServiceStatusEnum.DISCONNECT)) {
                log.error("服务状态非断开，name:{}", name);
                return;
            }

            log.info("服务名:{},地址:{},端口:{},服务被移除", name, service.getIpAddress(), service.getPort());

            this.serviceMap.remove(serviceId);
            this.suspected.remove(service.getApplicationId());
            List<Service> services = serviceList.get(name);

            if (CollUtil.isNotEmpty(services)) {
                for (int i = services.size() - 1; i >= 0; i--) {
                    Service s = services.get(i);
                    if (s.getServiceId().equalsIgnoreCase(serviceId)) {
                        services.remove(i);
                    }
                }
            }

            this.gatewayCircuitBreakerManager.remove(serviceId);
            this.attachmentCache.remove(this.getServiceRedisKey(RedisConstants.Gateway.HEARTBEAT_FAIL_COUNT, service));
            this.refreshVersion(name);
            this.warn(ServiceWarnTypeEnum.PROVIDER_OFFLINE, service);
        } finally {
            lock.unlock();
        }
    }

    public List<Service> getServiceList(String name, String version) {
        Set<String> versions = serviceVersion.get(name);
        if (CollUtil.isEmpty(versions)) {
            return Lists.newArrayList();
        }

        List<Service> services = serviceList.get(name);

        if (!versions.contains(version)) {
            return Lists.newArrayList();
        }

        return services.stream().filter(bean -> version.equals(bean.getVersion()) && !suspected.contains(bean.getApplicationId())).collect(Collectors.toList());
    }

    public void addInvokeFail(Service service) {
        log.info("服务疑似异常,name:{},ipAddress:{},port:{}", service.getName(), service.getIpAddress(), service.getPort());

        this.suspected.add(service.getApplicationId());
    }

    public void manualUpdateService(AdjustServiceArgumentVO adjustServiceArgumentVO) {
        Service service = serviceMap.get(adjustServiceArgumentVO.getServiceId());
        if (service == null) {
            throw new GatewayException("服务不存在");
        }

        service.setWeight(adjustServiceArgumentVO.getWeight());
        gatewayServiceProviderService.update(service, true);
    }

    public void manualUpdateMethod(AdjustMethodArgumentVO adjustMethodArgumentVO) {
        Service service = serviceMap.get(adjustMethodArgumentVO.getServiceId());
        if (service == null) {
            throw new GatewayException("服务不存在");
        }

        List<Method> methods = service.getMethods();
        if (CollUtil.isEmpty(methods)) {
            throw new GatewayException("目标方法不存在");
        }

        Optional<Method> first = methods.stream().filter(bean -> bean.getMethodId().equals(adjustMethodArgumentVO.getMethodId())).findFirst();
        if (!first.isPresent()) {
            throw new GatewayException("目标方法不存在");
        }

        GatewayServiceMethod query = gatewayServiceMethodService.query(service, first.get());
        if (query == null) {
            throw new GatewayException("目标方法不存在");
        }

        Integer timeout = adjustMethodArgumentVO.getTimeout();
        Integer retry = adjustMethodArgumentVO.getRetry();
        if (!query.getTimeout().equals(timeout) || !query.getRetry().equals(retry)) {
            query.setRetry(retry);
            query.setTimeout(timeout);
            query.setIsManual(YesOrNoEnum.YES.getCode());
        }

        gatewayServiceMethodService.updateById(query);
    }

    public boolean reload(Service service, String id) {
        String name = service.getName();
        String serviceId = this.getServiceId(service);

        Service exist = serviceMap.get(serviceId);
        if (exist != null) {
            exist.setApplicationId(service.getApplicationId());
            exist.setStatus(ServiceStatusEnum.ALIVE);
            return false;
        }

        log.info("服务重载,name:{},ipAddress:{},port:{}", service.getName(), service.getIpAddress(), service.getPort());

        Lock lock = stripedLock.getLocalLock(stripedLockName, 16, serviceId);

        lock.lock();
        try {
            service.setStatus(ServiceStatusEnum.ALIVE);

            List<Service> put = serviceList.putIfAbsent(name, Lists.newArrayList(service));
            if (put != null) {
                put.add(service);
            }

            this.serviceMap.put(serviceId, service);
            this.suspected.remove(service.getApplicationId());
            this.attachmentCache.remove(this.getServiceRedisKey(RedisConstants.Gateway.HEARTBEAT_FAIL_COUNT, service));
            this.refreshVersion(name);
            this.registerService(service, id);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void register(Service service, String id) {
        log.info("收到注册信息,name:{},ipAddress:{},port:{}", service.getName(), service.getIpAddress(), service.getPort());

        String name = service.getName();
        String serviceId = this.getServiceId(service);

        Lock lock = stripedLock.getLocalLock(stripedLockName, 16, serviceId);

        lock.lock();
        try {
            service.setStatus(ServiceStatusEnum.ALIVE);

            Service exist = serviceMap.get(serviceId);
            if (exist != null) {
                exist.getReferenceCount().set(0);
                exist.setApplicationId(service.getApplicationId());
                exist.setStatus(ServiceStatusEnum.ALIVE);
                this.registerService(service, id);
                this.attachmentCache.remove(RedisConstants.Gateway.SERVICE_USE_STATUS + exist.getServiceId());
                return;
            }

            List<Service> put = serviceList.putIfAbsent(name, Lists.newArrayList(service));
            if (put != null) {
                put.add(service);
            }

            this.serviceMap.put(serviceId, service);
            this.suspected.remove(service.getApplicationId());
            this.attachmentCache.remove(this.getServiceRedisKey(RedisConstants.Gateway.HEARTBEAT_FAIL_COUNT, service));
            this.refreshVersion(name);
            this.registerService(service, id);
        } finally {
            lock.unlock();
        }
    }

    public void releaseService(String serviceId) {
        Service service = this.serviceMap.get(serviceId);
        if (service != null) {
            service.release();
        }

        this.freeService(serviceId);
    }

    public void freeService(String serviceId) {
        this.attachmentCache.remove(RedisConstants.Gateway.SERVICE_USE_STATUS + serviceId);
    }

    public Boolean lockService(String serviceId) {
        return attachmentCache.setIfAbsent(RedisConstants.Gateway.SERVICE_USE_STATUS + serviceId, YesOrNoEnum.YES.getCode());
    }

    public String getServiceId(String name, String ipAddress, Integer port, String version) {
        return SecureUtil.md5(name + ipAddress + port + version);
    }

    /**
     * 警告
     *
     * @param serviceWarnTypeEnum
     * @param service
     */
    private void warn(ServiceWarnTypeEnum serviceWarnTypeEnum, Service service) {
        GatewayServiceProvider query = gatewayServiceProviderService.query(service);
        if (query == null) {
            return;
        }

        ServiceWarnEvent serviceWarnEvent = new ServiceWarnEvent(applicationContext);
        serviceWarnEvent.setServiceId(query.getServiceId());
        serviceWarnEvent.setProviderId(query.getId());
        serviceWarnEvent.setServiceWarnType(serviceWarnTypeEnum);
        serviceWarnEvent.setCreateDate(new Date());
        applicationContext.publishEvent(serviceWarnEvent);
    }

    /**
     * 获取服务id
     *
     * @param service
     * @return
     */
    private String getServiceId(Service service) {
        String serviceId = service.getServiceId();
        if (StrUtil.isNotEmpty(serviceId)) {
            return serviceId;
        }

        serviceId = this.getServiceId(service.getName(), service.getIpAddress(), service.getPort(), service.getVersion());
        service.setServiceId(serviceId);
        return serviceId;
    }

    /**
     * 注册服务
     *
     * @param service
     * @param id
     */
    private void registerService(Service service, String id) {
        if (Boolean.TRUE.equals(this.attachmentCache.setIfAbsent(RedisConstants.Gateway.SERVICE_REGISTER_FLAG + id, YesOrNoEnum.YES.getCode(), 60L, TimeUnit.SECONDS))) {

            try {
                GatewayService gatewayService = this.gatewayServiceService.getGatewayService(service);

                Lock lock = stripedLock.getLocalLock("service", 8, gatewayService.getId());
                try {
                    lock.lock();

                    this.gatewayServiceProviderService.register(gatewayService, service);
                    this.gatewayServiceMethodParamService.refreshMethod(gatewayService, service);
                } finally {
                    lock.unlock();
                }
            } finally {
                this.attachmentCache.remove(RedisConstants.Gateway.SERVICE_REGISTER_FLAG + id);
            }
        }
    }

    /**
     * 刷新
     *
     * @param name
     */
    private void refreshVersion(String name) {
        List<Service> services = this.serviceList.get(name);
        if (CollUtil.isEmpty(services)) {
            return;
        }

        this.serviceVersion.put(name, services.stream().map(Service::getVersion).collect(Collectors.toSet()));
    }

    /**
     * 获取服务redis key
     *
     * @param service
     * @return
     */
    private String getServiceRedisKey(String prefix, Service service) {
        return StrUtil.builder().append(prefix).append(this.id).append(":").append(service.getType()).append(":").append(service.getName()).toString();
    }
}
