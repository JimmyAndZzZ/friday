package com.jimmy.friday.center.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.gateway.OrderPurposeTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderSourceTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderStatusEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.entity.*;
import com.jimmy.friday.center.service.*;
import com.jimmy.friday.center.utils.RedisConstants;
import com.jimmy.friday.center.vo.PageInfoVO;
import com.jimmy.friday.center.vo.Result;
import com.jimmy.friday.center.vo.gateway.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/open")
@Api(tags = "管理API")
@Slf4j
public class OpenController {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Autowired
    private GatewayAccountOrderService gatewayAccountOrderService;

    @Autowired
    private GatewayCostStrategyService gatewayCostStrategyService;

    @Autowired
    private GatewayServiceMethodOpenService gatewayServiceMethodOpenService;

    @Autowired
    private GatewayAccountInvokeCountService gatewayAccountInvokeCountService;

    @Autowired
    private GatewayCostStrategyDetailsService gatewayCostStrategyDetailsService;

    @PostMapping("/register/{name}")
    @ApiOperation("注册")
    public Result<?> register(@PathVariable("name") String name) {
        GatewayAccount register = gatewayAccountService.register(name);

        Map<String, Object> result = Maps.newHashMap();
        result.put("appId", register.getUid());
        result.put("appSecretKey", register.getSeckey());
        return Result.ok(result);
    }

    @GetMapping("/apiList")
    @ApiOperation("获取api列表")
    public Result<?> apiList() {
        List<GatewayServiceMethodOpen> list = gatewayServiceMethodOpenService.queryList();
        return Result.ok(CollUtil.isNotEmpty(list) ? list.stream().map(bean -> BeanUtil.toBean(bean, OpenMethodVO.class)).collect(Collectors.toList()) : Lists.newArrayList());
    }

    @GetMapping("/getRechargeRecord")
    @ApiOperation("获取充值列表")
    public Result<?> getRechargeRecord(@RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize, @RequestParam("appId") String appId) {
        IPage<GatewayAccountOrder> page = gatewayAccountOrderService.page(pageNo, pageSize, OrderSourceTypeEnum.INSIDE, OrderPurposeTypeEnum.BALANCE_RECHARGE, OrderStatusEnum.PAID, appId);
        return Result.ok(PageInfoVO.build(page, OrderVO.class));
    }

    @GetMapping("/person")
    @ApiOperation("获取个人信息")
    public Result<?> person(@RequestParam("appId") String appId) {
        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        return Result.ok(gatewayAccount != null ? BeanUtil.toBean(gatewayAccount, AccountVO.class) : null);
    }

    @GetMapping("/getTodayInvokeCount")
    @ApiOperation("获取今日调用量")
    public Result<?> getTodayInvokeCount(@RequestParam("appId") String appId) {
        String s = attachmentCache.attachment(RedisConstants.Gateway.TODAY_INVOKE_COUNT + appId);
        return Result.ok(StrUtil.isEmpty(s) ? 0 : Convert.toInt(s, 0));
    }

    @GetMapping("/getTodayCostAmount")
    @ApiOperation("获取今日消费")
    public Result<?> getTodayCostAmount(@RequestParam("appId") String appId) {
        return Result.ok(gatewayAccountService.getTodayCostAmount(appId));
    }

    @GetMapping("/getMonthInvokeCount")
    @ApiOperation("获取一个月调用量")
    public Result<?> getMonthInvokeCount(@RequestParam("appId") String appId) {
        return Result.ok(gatewayAccountInvokeCountService.getMonthInvokeCount(appId));
    }

    @GetMapping("/chargeDescription")
    @ApiOperation("收费说明")
    public Result<?> chargeDescription(@RequestParam("id") Long id) {
        GatewayServiceMethodOpen byId = gatewayServiceMethodOpenService.getById(id);
        if (byId == null) {
            return Result.ok();
        }

        Long costStrategyId = byId.getCostStrategyId();
        if (costStrategyId == null || YesOrNoEnum.YES.getCode().equalsIgnoreCase(byId.getIsFree())) {
            return Result.ok();
        }

        GatewayCostStrategy costStrategy = gatewayCostStrategyService.queryById(costStrategyId);
        if (costStrategy == null) {
            return Result.ok();
        }

        CostStrategyVO vo = new CostStrategyVO();
        vo.setChargeType(costStrategy.getChargeType());
        vo.setId(costStrategy.getId());
        vo.setName(costStrategy.getName());
        vo.setName(costStrategy.getName());

        List<GatewayCostStrategyDetails> gatewayCostStrategyDetails = gatewayCostStrategyDetailsService.queryByCostStrategyId(costStrategyId);
        if (CollUtil.isNotEmpty(gatewayCostStrategyDetails)) {
            for (GatewayCostStrategyDetails gatewayCostStrategyDetail : gatewayCostStrategyDetails) {
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

        return Result.ok(vo);
    }
}
