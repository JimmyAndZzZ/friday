package com.jimmy.friday.center.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.InvokeMetricsTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.Gateway;
import com.jimmy.friday.center.core.gateway.RegisterCenter;
import com.jimmy.friday.center.entity.*;
import com.jimmy.friday.center.service.*;
import com.jimmy.friday.center.core.gateway.support.RegisterSupport;
import com.jimmy.friday.center.utils.JsonUtil;
import com.jimmy.friday.center.vo.*;
import com.jimmy.friday.center.vo.gateway.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service")
@Api(tags = "服务API")
@Slf4j
public class ServiceController {

    @Autowired
    private Gateway gateway;

    @Autowired
    private RegisterSupport registerSupport;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayInvokeTraceService gatewayInvokeTraceService;

    @Autowired
    private GatewayServiceWarnService gatewayServiceWarnService;

    @Autowired
    private GatewayCostStrategyService gatewayCostStrategyService;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Autowired
    private GatewayServiceConsumerService gatewayServiceConsumerService;

    @Autowired
    private GatewayServiceProviderService gatewayServiceProviderService;

    @Autowired
    private GatewayServiceMethodOpenService gatewayServiceMethodOpenService;

    @Autowired
    private GatewayServiceMethodParamService gatewayServiceMethodParamService;

    @Autowired
    private GatewayCostStrategyDetailsService gatewayCostStrategyDetailsService;

    @Autowired
    private GatewayServiceMethodInvokeMetricsService gatewayServiceMethodInvokeMetricsService;

    @GetMapping("/getGroupNames")
    @ApiOperation("获取组名列表")
    public Result<?> getGroupNames() {
        return Result.ok(gatewayServiceService.getGroupNames());
    }

    @PostMapping("/editOpenMethod")
    @ApiOperation("编辑对外API")
    public Result<?> editOpenMethod(@RequestBody MethodOpenVO methodOpenVO) {
        Long id = methodOpenVO.getId();
        String code = methodOpenVO.getCode();
        Long methodId = methodOpenVO.getMethodId();
        Long costStrategyId = methodOpenVO.getCostStrategyId();

        GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.queryById(methodId);
        if (gatewayServiceMethod == null) {
            return Result.error("方法不存在");
        }

        GatewayCostStrategy gatewayCostStrategy = gatewayCostStrategyService.queryById(costStrategyId);
        if (gatewayCostStrategy == null) {
            return Result.error("收费策略不存在");
        }

        if (id == null) {
            GatewayServiceMethodOpen exist = gatewayServiceMethodOpenService.queryByCode(code);
            if (exist != null) {
                return Result.error("该code已存在");
            }

            GatewayServiceMethodOpen gatewayServiceMethodOpen = new GatewayServiceMethodOpen();
            gatewayServiceMethodOpen.setMethodId(methodOpenVO.getMethodId());
            gatewayServiceMethodOpen.setCode(methodOpenVO.getCode());
            gatewayServiceMethodOpen.setExample(methodOpenVO.getExample());
            gatewayServiceMethodOpen.setType(methodOpenVO.getType());
            gatewayServiceMethodOpen.setIsFree(methodOpenVO.getIsFree());
            gatewayServiceMethodOpen.setStatus(methodOpenVO.getStatus());
            gatewayServiceMethodOpen.setDescription(methodOpenVO.getDescription());
            gatewayServiceMethodOpen.setName(methodOpenVO.getName());
            gatewayServiceMethodOpen.setServiceId(gatewayServiceMethod.getServiceId());
            gatewayServiceMethodOpen.setCostStrategyId(costStrategyId);
            gatewayServiceMethodOpenService.save(gatewayServiceMethodOpen);
        } else {
            GatewayServiceMethodOpen gatewayServiceMethodOpen = new GatewayServiceMethodOpen();
            gatewayServiceMethodOpen.setExample(methodOpenVO.getExample());
            gatewayServiceMethodOpen.setType(methodOpenVO.getType());
            gatewayServiceMethodOpen.setIsFree(methodOpenVO.getIsFree());
            gatewayServiceMethodOpen.setStatus(methodOpenVO.getStatus());
            gatewayServiceMethodOpen.setDescription(methodOpenVO.getDescription());
            gatewayServiceMethodOpen.setName(methodOpenVO.getName());
            gatewayServiceMethodOpen.setCostStrategyId(costStrategyId);
            gatewayServiceMethodOpenService.updateById(gatewayServiceMethodOpen);
        }

        return Result.ok();
    }

    @GetMapping("/getCostStrategyList")
    @ApiOperation("获取计费策略列表")
    public Result<?> getCostStrategyList() {
        List<CostStrategyVO> costStrategyVOS = Lists.newArrayList();

        List<GatewayCostStrategy> list = gatewayCostStrategyService.list();
        if (CollUtil.isNotEmpty(list)) {
            List<GatewayCostStrategyDetails> gatewayCostStrategyDetails = gatewayCostStrategyDetailsService.queryByCostStrategyIds(list.stream().map(GatewayCostStrategy::getId).collect(Collectors.toSet()));

            Map<Long, List<GatewayCostStrategyDetails>> groupBy = CollUtil.isNotEmpty(gatewayCostStrategyDetails) ? gatewayCostStrategyDetails.stream().collect(Collectors.groupingBy(GatewayCostStrategyDetails::getStrategyId)) : Maps.newHashMap();

            for (GatewayCostStrategy costStrategy : list) {
                Long id = costStrategy.getId();

                CostStrategyVO vo = new CostStrategyVO();
                vo.setChargeType(costStrategy.getChargeType());
                vo.setId(id);
                vo.setName(costStrategy.getName());
                vo.setName(costStrategy.getName());

                List<GatewayCostStrategyDetails> values = groupBy.get(id);
                if (CollUtil.isNotEmpty(values)) {
                    for (GatewayCostStrategyDetails gatewayCostStrategyDetail : values) {
                        CostStrategyDetailsVO detailsVO = new CostStrategyDetailsVO();
                        detailsVO.setId(gatewayCostStrategyDetail.getId());
                        detailsVO.setPrice(gatewayCostStrategyDetail.getPrice());
                        detailsVO.setThresholdValue(gatewayCostStrategyDetail.getThresholdValue());
                        vo.getDetails().add(detailsVO);
                    }

                    if (vo.getDetails().size() > 1) {
                        vo.getDetails().sort(Comparator.comparing(CostStrategyDetailsVO::getThresholdValue));
                    }
                }

                costStrategyVOS.add(vo);
            }
        }

        return Result.ok(costStrategyVOS);
    }

    @PostMapping("/debug")
    @ApiOperation("调试")
    public Result<?> debug(@RequestBody DebugVO debugVO) {
        String json = debugVO.getJson();
        //方法查询
        GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.getById(debugVO.getMethodId());
        if (gatewayServiceMethod == null) {
            return Result.error("方法不存在");
        }
        //服务查询
        GatewayService gatewayService = gatewayServiceService.queryById(gatewayServiceMethod.getServiceId());
        if (gatewayService == null) {
            return Result.error("服务不存在");
        }

        GatewayRequest gatewayRequest = new GatewayRequest();
        gatewayRequest.setId(IdUtil.getSnowflake(1, 1).nextId());
        gatewayRequest.setAppId(debugVO.getAppId());
        gatewayRequest.setClientName("DEBUG");
        gatewayRequest.setIsDebug(true);
        gatewayRequest.setVersion(gatewayService.getVersion());
        gatewayRequest.setServiceType(gatewayService.getType());
        gatewayRequest.setRetry(gatewayServiceMethod.getRetry());
        gatewayRequest.setMethodName(gatewayServiceMethod.getName());
        gatewayRequest.setTimeout(gatewayServiceMethod.getTimeout());
        gatewayRequest.setMethodId(gatewayServiceMethod.getMethodId());
        gatewayRequest.setMethodCode(gatewayServiceMethod.getMethodCode());
        gatewayRequest.setServiceName(gatewayService.getApplicationName());
        gatewayRequest.setClientIpAddress(NetUtil.getLocalhostStr());
        gatewayRequest.setInvokeInterface(gatewayServiceMethod.getInterfaceName());

        if (StrUtil.isNotEmpty(json)) {
            JsonNode parse = JsonUtil.parse(json);

            if (parse != null) {
                Iterator<String> names = parse.fieldNames();

                while (names.hasNext()) {
                    String fieldName = names.next();
                    JsonNode childNode = parse.get(fieldName);

                    InvokeParam invokeParam = new InvokeParam();
                    invokeParam.setName(fieldName);
                    invokeParam.setJsonData(childNode.toString());
                    gatewayRequest.getInvokeParams().add(invokeParam);
                }
            }
        }

        try {
            GatewayResponse run = gateway.run(gatewayRequest);
            return run.getIsSuccess() ? Result.ok(run.getJsonResult()) : Result.error(run.getError());
        } catch (Throwable e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/getMethodDetail")
    @ApiOperation("获取方法详情")
    public Result<?> getMethodDetail(@RequestParam("id") Long methodId) {
        GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.queryById(methodId);
        if (gatewayServiceMethod == null) {
            return Result.ok();
        }

        MethodDetailVO methodVO = new MethodDetailVO();
        methodVO.setServiceId(gatewayServiceMethod.getServiceId());
        methodVO.setId(methodId);
        methodVO.setName(gatewayServiceMethod.getName());
        methodVO.setMethodId(gatewayServiceMethod.getMethodId());
        methodVO.setRetry(gatewayServiceMethod.getRetry());
        methodVO.setTimeout(gatewayServiceMethod.getTimeout());
        methodVO.setInterfaceName(gatewayServiceMethod.getInterfaceName());
        methodVO.setParamType(gatewayServiceMethod.getParamType());
        methodVO.setReturnType(gatewayServiceMethod.getReturnType());
        methodVO.setTodayInvokeCount(gatewayServiceMethodService.getTodayMethodInvokeCount(methodId));
        methodVO.setHistoryInvokeCount(gatewayServiceMethodService.getHistoryMethodInvokeCount(methodId) + methodVO.getTodayInvokeCount());

        Long lastInvokeTimestamp = gatewayServiceMethodService.getLastInvokeTimestamp(methodId);
        if (lastInvokeTimestamp != null) {
            methodVO.setLastInvokeDate(new Date(lastInvokeTimestamp));
        }

        List<GatewayServiceMethodParam> methodParams = gatewayServiceMethodParamService.queryByMethodId(methodId);

        if (CollUtil.isNotEmpty(methodParams)) {
            for (GatewayServiceMethodParam param : methodParams) {
                MethodParamVO methodParamVO = new MethodParamVO();
                methodParamVO.setDesc(param.getDesc());
                methodParamVO.setParamType(param.getParamType());
                methodParamVO.setName(param.getName());
                methodParamVO.setId(param.getId());
                methodVO.getMethodParams().add(methodParamVO);
            }
        }

        methodVO.setEverydayMetrics(gatewayServiceMethodInvokeMetricsService.getMetrics(methodId, InvokeMetricsTypeEnum.EVERYDAY));
        methodVO.setHistoryMetrics(gatewayServiceMethodInvokeMetricsService.getMetrics(methodId, InvokeMetricsTypeEnum.HISTORY));

        GatewayServiceMethodOpen gatewayServiceMethodOpen = gatewayServiceMethodOpenService.queryByMethodId(methodId);
        if (gatewayServiceMethodOpen != null) {
            Long costStrategyId = gatewayServiceMethodOpen.getCostStrategyId();

            MethodOpenVO methodOpenVO = new MethodOpenVO();
            methodOpenVO.setMethodId(gatewayServiceMethodOpen.getMethodId());
            methodOpenVO.setCode(gatewayServiceMethodOpen.getCode());
            methodOpenVO.setExample(gatewayServiceMethodOpen.getExample());
            methodOpenVO.setType(gatewayServiceMethodOpen.getType());
            methodOpenVO.setId(gatewayServiceMethodOpen.getId());
            methodOpenVO.setIsFree(gatewayServiceMethodOpen.getIsFree());
            methodOpenVO.setStatus(gatewayServiceMethodOpen.getStatus());
            methodOpenVO.setDescription(gatewayServiceMethodOpen.getDescription());
            methodOpenVO.setName(gatewayServiceMethodOpen.getName());

            GatewayCostStrategy byId = gatewayCostStrategyService.queryById(costStrategyId);
            if (byId != null) {
                methodOpenVO.setCostStrategyId(costStrategyId);
                methodOpenVO.setCostStrategyName(byId.getName());
            }

            methodVO.setMethodOpenDetail(methodOpenVO);
        }


        return Result.ok(methodVO);
    }

    @GetMapping("/getServiceDetail")
    @ApiOperation("获取服务详情")
    public Result<?> getServiceDetail(@RequestParam("id") Long serviceId) {
        GatewayService gatewayService = gatewayServiceService.queryById(serviceId);
        if (gatewayService == null) {
            return Result.ok();
        }

        long lastServiceInvokeTimestamp = 0L;

        ServiceVO serviceVO = new ServiceVO();
        serviceVO.setId(serviceId);
        serviceVO.setApplicationName(gatewayService.getApplicationName());
        serviceVO.setType(gatewayService.getType());
        serviceVO.setDescription(gatewayService.getDescription());
        serviceVO.setVersion(gatewayService.getVersion());

        List<GatewayServiceWarn> gatewayServiceWarns = gatewayServiceWarnService.queryList(serviceId, null);
        if (CollUtil.isNotEmpty(gatewayServiceWarns)) {
            for (GatewayServiceWarn gatewayServiceWarn : gatewayServiceWarns) {
                ServiceWarnVO serviceWarnVO = new ServiceWarnVO();
                serviceWarnVO.setType(gatewayServiceWarn.getType());
                serviceWarnVO.setCreateDate(gatewayServiceWarn.getCreateDate());
                serviceWarnVO.setMessage(gatewayServiceWarn.getMessage());
                serviceVO.getWarns().add(serviceWarnVO);
            }
        }

        List<GatewayServiceConsumer> gatewayServiceConsumers = gatewayServiceConsumerService.queryByServiceId(serviceId);
        if (CollUtil.isNotEmpty(gatewayServiceConsumers)) {
            Set<Long> providerIds = gatewayServiceConsumers.stream().filter(bean -> bean.getProviderId() != null).map(GatewayServiceConsumer::getProviderId).collect(Collectors.toSet());

            Collection<GatewayServiceProvider> gatewayServiceProviders = CollUtil.isEmpty(providerIds) ? Lists.newArrayList() : gatewayServiceProviderService.listByIds(providerIds);
            Map<Long, GatewayServiceProvider> collect = CollUtil.isEmpty(gatewayServiceProviders) ? Maps.newHashMap() : gatewayServiceProviders.stream().collect(Collectors.toMap(GatewayServiceProvider::getId, g -> g));

            for (GatewayServiceConsumer gatewayServiceConsumer : gatewayServiceConsumers) {
                ConsumerVO consumerVO = new ConsumerVO();
                consumerVO.setServiceId(gatewayServiceConsumer.getServiceId());
                consumerVO.setAppId(gatewayServiceConsumer.getAppId());
                consumerVO.setClientName(gatewayServiceConsumer.getClientName());
                consumerVO.setCreateDate(gatewayServiceConsumer.getCreateDate());
                consumerVO.setIpAddress(gatewayServiceConsumer.getIpAddress());

                Long providerId = gatewayServiceConsumer.getProviderId();
                if (providerId != null) {
                    GatewayServiceProvider gatewayServiceProvider = collect.get(providerId);
                    if (gatewayServiceProvider != null) {
                        ProviderVO providerVO = new ProviderVO();
                        providerVO.setId(gatewayServiceProvider.getId());
                        providerVO.setServiceId(gatewayServiceProvider.getServiceId());
                        providerVO.setPort(gatewayServiceProvider.getPort());
                        providerVO.setWeight(gatewayServiceProvider.getWeight());
                        providerVO.setIpAddress(gatewayServiceProvider.getIpAddress());
                        providerVO.setStatus(gatewayServiceProvider.getStatus());
                        providerVO.setCreateDate(gatewayServiceProvider.getCreateDate());
                        consumerVO.setLastInvokeProvider(providerVO);
                    }
                }

                serviceVO.getConsumers().add(consumerVO);
            }
        }

        List<GatewayService> gatewayServices = gatewayServiceService.queryByApplicationNameAndType(gatewayService.getApplicationName(), gatewayService.getType());
        for (GatewayService service : gatewayServices) {
            if (service.getId().equals(serviceId)) {
                continue;
            }

            ServiceVO other = new ServiceVO();
            other.setId(service.getId());
            other.setVersion(service.getVersion());
            serviceVO.getOtherVersions().add(other);
        }

        List<GatewayServiceMethod> serviceMethods = gatewayServiceMethodService.queryByServiceId(serviceId);
        if (CollUtil.isNotEmpty(serviceMethods)) {
            Set<Long> longs = gatewayServiceMethodOpenService.queryOpenMethodIds(serviceMethods.stream().map(GatewayServiceMethod::getId).collect(Collectors.toSet()));

            for (GatewayServiceMethod serviceMethod : serviceMethods) {
                Long id = serviceMethod.getId();

                MethodVO methodVO = new MethodVO();
                methodVO.setServiceId(serviceMethod.getServiceId());
                methodVO.setId(id);
                methodVO.setIsOpen(longs.contains(id));
                methodVO.setName(serviceMethod.getName());
                methodVO.setMethodId(serviceMethod.getMethodId());
                methodVO.setRetry(serviceMethod.getRetry());
                methodVO.setTimeout(serviceMethod.getTimeout());
                methodVO.setInterfaceName(serviceMethod.getInterfaceName());
                methodVO.setParamType(serviceMethod.getParamType());
                methodVO.setReturnType(serviceMethod.getReturnType());
                methodVO.setTodayInvokeCount(gatewayServiceMethodService.getTodayMethodInvokeCount(id));
                methodVO.setHistoryInvokeCount(gatewayServiceMethodService.getHistoryMethodInvokeCount(id) + methodVO.getTodayInvokeCount());

                Long lastInvokeTimestamp = gatewayServiceMethodService.getLastInvokeTimestamp(id);
                if (lastInvokeTimestamp != null && lastInvokeTimestamp > 0) {
                    if (lastInvokeTimestamp > lastServiceInvokeTimestamp) {
                        lastServiceInvokeTimestamp = lastInvokeTimestamp;
                    }

                    methodVO.setLastInvokeDate(new Date(lastInvokeTimestamp));
                }

                serviceVO.getMethods().add(methodVO);
            }
        }

        List<GatewayServiceProvider> serviceProviders = gatewayServiceProviderService.queryByServiceId(serviceId);
        if (CollUtil.isNotEmpty(serviceProviders)) {
            for (GatewayServiceProvider serviceProvider : serviceProviders) {
                ProviderVO providerVO = new ProviderVO();
                providerVO.setId(serviceProvider.getId());
                providerVO.setServiceId(serviceProvider.getServiceId());
                providerVO.setPort(serviceProvider.getPort());
                providerVO.setWeight(serviceProvider.getWeight());
                providerVO.setIpAddress(serviceProvider.getIpAddress());
                providerVO.setStatus(serviceProvider.getStatus());
                providerVO.setCreateDate(serviceProvider.getCreateDate());
                serviceVO.getProviders().add(providerVO);
            }
        }

        if (lastServiceInvokeTimestamp > 0) {
            serviceVO.setLastInvokeDate(new Date(lastServiceInvokeTimestamp));
        }

        return Result.ok(serviceVO);
    }

    @GetMapping("/getInvokeTracePageList")
    @ApiOperation("获取调用流水")
    public Result<?> getInvokeTracePageList(@RequestParam(value = "serviceId", required = false) Long serviceId, @RequestParam(value = "providerId", required = false) Long providerId, @RequestParam(value = "methodId", required = false) Long methodId, @RequestParam(value = "startDate", required = false) String startDate, @RequestParam(value = "endDate", required = false) String endDate, @RequestParam(value = "appId", required = false) String appId, @RequestParam(value = "isSuccess", required = false) Boolean isSuccess, @RequestParam(value = "pageNo") Integer pageNo, @RequestParam(value = "pageSize") Integer pageSize) {
        IPage<GatewayInvokeTrace> page = gatewayInvokeTraceService.page(serviceId, providerId, methodId, startDate, endDate, appId, isSuccess, pageNo, pageSize);

        return Result.ok(PageInfoVO.build(page, InvokeTraceVO.class));
    }

    @PostMapping("/updateService")
    @ApiOperation("更新服务")
    public Result<?> updateService(@RequestBody UpdateServiceVO updateServiceVO) {
        GatewayService gatewayService = gatewayServiceService.getById(updateServiceVO.getId());
        if (gatewayService == null) {
            return Result.error("服务不存在");
        }

        List<GatewayService> gatewayServices = gatewayServiceService.queryByApplicationNameAndType(gatewayService.getApplicationName(), gatewayService.getType());
        for (GatewayService service : gatewayServices) {
            service.setGroupName(updateServiceVO.getGroupName());
            service.setDescription(updateServiceVO.getDescription());
        }

        gatewayServiceService.updateBatchById(gatewayServices);
        return Result.ok();
    }

    @GetMapping("/getServiceList")
    @ApiOperation("获取服务列表")
    public Result<?> getServiceList(@RequestParam(value = "groupName", required = false) String groupName,
                                    @RequestParam(value = "serviceType", required = false) ServiceTypeEnum serviceType) {
        List<GatewayService> list = gatewayServiceService.queryList(groupName, serviceType);
        if (CollUtil.isEmpty(list)) {
            return Result.ok(Lists.newArrayList());
        }

        List<ServiceVO> services = Lists.newArrayList();

        for (GatewayService gatewayService : list) {
            ServiceVO serviceVO = new ServiceVO();
            serviceVO.setId(gatewayService.getId());
            serviceVO.setApplicationName(gatewayService.getApplicationName());
            serviceVO.setType(gatewayService.getType());
            serviceVO.setDescription(gatewayService.getDescription());
            serviceVO.setVersion(gatewayService.getVersion());
            services.add(serviceVO);
        }

        return Result.ok(services);
    }

    @PostMapping("/adjustServiceArgument")
    @ApiOperation("调整服务参数")
    public Result<?> adjustServiceArgument(@RequestBody AdjustServiceArgumentVO adjustServiceArgumentVO) {
        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.queryByType(adjustServiceArgumentVO.getServiceType());
        if (serviceTypeEnum == null) {
            return Result.error("服务类型异常");
        }

        RegisterCenter center = registerSupport.get(serviceTypeEnum);
        if (center == null) {
            return Result.error("服务类型不存在");
        }

        center.manualUpdateService(adjustServiceArgumentVO);
        return Result.ok();
    }

    @PostMapping("/adjustMethodArgument")
    @ApiOperation("调整方法参数")
    public Result<?> adjustMethodArgument(@RequestBody AdjustMethodArgumentVO adjustMethodArgumentVO) {
        ServiceTypeEnum serviceTypeEnum = ServiceTypeEnum.queryByType(adjustMethodArgumentVO.getServiceType());
        if (serviceTypeEnum == null) {
            return Result.error("服务类型异常");
        }

        RegisterCenter center = registerSupport.get(serviceTypeEnum);
        if (center == null) {
            return Result.error("服务类型不存在");
        }

        center.manualUpdateMethod(adjustMethodArgumentVO);
        return Result.ok();
    }
}
