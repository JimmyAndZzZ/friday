package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.enums.gateway.OrderPurposeTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderSourceTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderStatusEnum;
import com.jimmy.friday.center.entity.GatewayAccountOrder;

import java.math.BigDecimal;

/**
 * (GatewayAccountOrder)表服务接口
 *
 * @author makejava
 * @since 2024-01-09 14:22:05
 */
public interface GatewayAccountOrderService extends IService<GatewayAccountOrder> {

    IPage<GatewayAccountOrder> page(Integer pageNo, Integer pageSize, OrderSourceTypeEnum orderSourceTypeEnum, OrderPurposeTypeEnum orderPurposeTypeEnum, OrderStatusEnum orderStatusEnum, String appId);

    void insideBalanceRecharge(String appId, BigDecimal amount);
}

