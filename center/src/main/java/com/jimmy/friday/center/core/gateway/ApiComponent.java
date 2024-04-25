package com.jimmy.friday.center.core.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.jimmy.friday.boot.core.gateway.GatewayRequest;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.boot.other.GlobalConstants;
import com.jimmy.friday.center.Gateway;
import com.jimmy.friday.center.api.ApiContext;
import com.jimmy.friday.center.api.ApiResponse;
import com.jimmy.friday.center.base.Hook;
import com.jimmy.friday.center.entity.*;
import com.jimmy.friday.center.service.*;
import com.jimmy.friday.center.support.CollectSupport;
import com.jimmy.friday.center.support.CostSupport;
import com.jimmy.friday.center.utils.Assert;
import com.jimmy.friday.center.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Component
public class ApiComponent {

    private static final String HTTP_HEADER_SIGN = "sign";

    private static final String HTTP_HEADER_APP_ID = "appId";


    @Autowired
    private Gateway gateway;

    @Autowired
    private CostSupport costSupport;

    @Autowired
    private CollectSupport collectSupport;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private InvokeDelayedComponent invokeDelayedComponent;

    @Autowired
    private GatewayCostStrategyService gatewayCostStrategyService;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Autowired
    private GatewayServiceMethodOpenService gatewayServiceMethodOpenService;

    public ApiResponse run(HttpServletRequest httpServletRequest, String action) throws Throwable {
        //创建上下文
        ApiContext apiContext = new ApiContext();
        //生成id
        try {
            Long id = IdUtil.getSnowflake(1, 1).nextId();

            apiContext.put(ApiConstants.CONTEXT_PARAM_TRACE_ID, id);

            String appId = httpServletRequest.getHeader(HTTP_HEADER_APP_ID);
            String sign = httpServletRequest.getHeader(HTTP_HEADER_SIGN);

            Assert.state(StrUtil.isNotEmpty(appId), ExceptionEnum.MISS_PARAMETER, "appId");
            Assert.state(StrUtil.isNotEmpty(sign), ExceptionEnum.MISS_PARAMETER, "sign");
            //账号验证
            GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
            Assert.state(gatewayAccount != null, ExceptionEnum.ACCOUNT_ERROR, "账号不存在");
            Assert.state(YesOrNoEnum.YES.getCode().equals(gatewayAccount.getStatus()), ExceptionEnum.ACCOUNT_ERROR, "账号已关闭");
            Assert.state(sign.equalsIgnoreCase(this.getSign(httpServletRequest, gatewayAccount)), ExceptionEnum.ERROR_PARAMETER, "签名校验失败");
            //api验证
            GatewayServiceMethodOpen gatewayServiceMethodOpen = gatewayServiceMethodOpenService.getByCode(action);
            Assert.state(gatewayServiceMethodOpen != null, ExceptionEnum.METHOD_ERROR, "服务方法不存在");
            Assert.state(YesOrNoEnum.YES.getCode().equals(gatewayServiceMethodOpen.getStatus()), ExceptionEnum.METHOD_ERROR, "服务已下线");
            //费用查询
            Long costStrategyId = gatewayServiceMethodOpen.getCostStrategyId();
            Assert.state(costStrategyId != null, ExceptionEnum.METHOD_ERROR, "服务费用计算异常");
            //费用策略查询
            GatewayCostStrategy gatewayCostStrategy = gatewayCostStrategyService.getById(costStrategyId);
            Assert.state(gatewayCostStrategy != null, ExceptionEnum.METHOD_ERROR, "服务费用计算异常");
            //服务查询
            GatewayService gatewayService = gatewayServiceService.getById(gatewayServiceMethodOpen.getServiceId());
            Assert.state(gatewayService != null, ExceptionEnum.SERVICE_ERROR, "服务不存在");
            //方法查询
            GatewayServiceMethod gatewayServiceMethod = gatewayServiceMethodService.getById(gatewayServiceMethodOpen.getMethodId());
            Assert.state(gatewayServiceMethod != null, ExceptionEnum.METHOD_ERROR, "服务方法不存在");
            //收集参数
            List<InvokeParam> collect = collectSupport.collect(gatewayServiceMethodOpen.getType(), httpServletRequest, apiContext);
            //计算费用
            BigDecimal calculate = YesOrNoEnum.YES.getCode().equalsIgnoreCase(gatewayServiceMethodOpen.getIsFree()) ? new BigDecimal(0) : costSupport.calculate(gatewayCostStrategy.getChargeType(), costStrategyId, appId, action, apiContext);
            //构建请求体
            GatewayRequest gatewayRequest = new GatewayRequest();
            gatewayRequest.setId(id);
            gatewayRequest.setIsApi(true);
            gatewayRequest.setAppId(appId);
            gatewayRequest.setCost(calculate);
            gatewayRequest.setInvokeParams(collect);
            gatewayRequest.setVersion(gatewayService.getVersion());
            gatewayRequest.setServiceType(gatewayService.getType());
            gatewayRequest.setRetry(gatewayServiceMethod.getRetry());
            gatewayRequest.setMethodName(gatewayServiceMethod.getName());
            gatewayRequest.setTimeout(gatewayServiceMethod.getTimeout());
            gatewayRequest.setMethodId(gatewayServiceMethod.getMethodId());
            gatewayRequest.setMethodCode(gatewayServiceMethod.getMethodCode());
            gatewayRequest.setServiceName(gatewayService.getApplicationName());
            gatewayRequest.setClientIpAddress(this.getIpAddress(httpServletRequest));
            gatewayRequest.setInvokeInterface(gatewayServiceMethod.getInterfaceName());
            return ApiResponse.ok(this.invoke(gatewayRequest, action, apiContext));
        } finally {
            List<Hook> hooks = apiContext.getHooks();
            if (CollUtil.isNotEmpty(hooks)) {
                for (Hook hook : hooks) {
                    hook.hook();
                }
            }
        }
    }

    /**
     * 网关调用
     *
     * @param gatewayRequest
     * @return
     */
    private Object invoke(GatewayRequest gatewayRequest, String action, ApiContext context) throws Throwable {
        Long id = gatewayRequest.getId();
        String appId = gatewayRequest.getAppId();
        BigDecimal cost = gatewayRequest.getCost();
        Integer timeout = gatewayRequest.getTimeout();
        //余额校验
        boolean isNeedDeductBalance = cost.compareTo(new BigDecimal(0)) > 0;

        if (isNeedDeductBalance) {
            //扣除费用
            Assert.state(gatewayAccountService.deductBalance(cost, appId), ExceptionEnum.ACCOUNT_BALANCE_INSUFFICIENT);
        }
        try {
            if (isNeedDeductBalance) {
                //发送延迟消息
                invokeDelayedComponent.sendDelayedMessage(id, timeout, new Date(), cost, appId);
            }

            return this.responseHandler(gateway.run(gatewayRequest));
        } catch (Throwable e) {
            if (isNeedDeductBalance) {
                gatewayAccountService.rollbackBalance(cost, appId);
            }

            costSupport.rollback(appId, action, context);

            throw e;
        }
    }

    /**
     * 获取签名
     *
     * @param httpServletRequest
     * @param gatewayAccount
     * @return
     */
    private String getSign(HttpServletRequest httpServletRequest, GatewayAccount gatewayAccount) {
        Date now = new Date();
        String timestamp = httpServletRequest.getParameter("timestamp");

        Assert.state(StrUtil.isNotEmpty(timestamp), ExceptionEnum.MISS_PARAMETER, "时间戳为空");
        Assert.state(Math.abs(now.getTime() - Convert.toLong(timestamp, 0L)) < GlobalConstants.Center.TIMESTAMP_MAX_DIFFERENCE, ExceptionEnum.ERROR_PARAMETER, "时间戳与当前时间超过5分钟");

        return SecureUtil.md5(StrUtil.format("appId:{};secretKey:{};timestamp:{}", gatewayAccount.getUid(), gatewayAccount.getSeckey(), timestamp));
    }

    /**
     * 返回值处理
     *
     * @param gatewayResponse
     * @return
     */
    private Object responseHandler(GatewayResponse gatewayResponse) throws Exception {
        String error = gatewayResponse.getError();
        Integer code = gatewayResponse.getCode();
        Boolean isSuccess = gatewayResponse.getIsSuccess();
        String jsonResult = gatewayResponse.getJsonResult();
        String exceptionClass = gatewayResponse.getExceptionClass();

        if (isSuccess) {
            JsonNode parse = JsonUtil.parse(jsonResult);
            return parse != null ? parse : jsonResult;
        }

        throw exceptionClass.equals(GatewayException.class.getName()) ? new GatewayException(code, error) : new Exception(error);
    }

    /**
     * 获取ip地址
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 如果使用了代理，则获取第一个IP地址
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }

        return ipAddress;
    }
}
