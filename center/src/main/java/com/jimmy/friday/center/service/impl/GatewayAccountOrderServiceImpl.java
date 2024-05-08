package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.gateway.OrderPurposeTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderSourceTypeEnum;
import com.jimmy.friday.boot.enums.gateway.OrderStatusEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.dao.GatewayAccountOrderDao;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.entity.GatewayAccountOrder;
import com.jimmy.friday.center.service.GatewayAccountOrderService;
import com.jimmy.friday.center.service.GatewayAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * (GatewayAccountOrder)表服务实现类
 *
 * @author makejava
 * @since 2024-01-09 14:22:05
 */
@Service("gatewayAccountOrderService")
public class GatewayAccountOrderServiceImpl extends ServiceImpl<GatewayAccountOrderDao, GatewayAccountOrder> implements GatewayAccountOrderService {

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Override
    public IPage<GatewayAccountOrder> page(Integer pageNo,
                                           Integer pageSize,
                                           OrderSourceTypeEnum orderSourceTypeEnum,
                                           OrderPurposeTypeEnum orderPurposeTypeEnum,
                                           OrderStatusEnum orderStatusEnum,
                                           String appId) {

        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            return new Page<>();
        }

        QueryWrapper<GatewayAccountOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", gatewayAccount.getId());

        if (orderSourceTypeEnum != null) {
            queryWrapper.eq("source", orderSourceTypeEnum.getCode());
        }

        if (orderPurposeTypeEnum != null) {
            queryWrapper.eq("purpose", orderPurposeTypeEnum.getCode());
        }

        if (orderStatusEnum != null) {
            queryWrapper.eq("status", orderStatusEnum.getCode());
        }

        queryWrapper.orderByDesc("create_date");

        return this.page(new Page<>(pageNo, pageSize), queryWrapper);
    }

    @Override
    public void insideBalanceRecharge(String appId, BigDecimal amount) {
        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            throw new GatewayException("账号不存在");
        }

        long traceNo = IdUtil.getSnowflake(1, 1).nextId();

        GatewayAccountOrder gatewayAccountOrder = new GatewayAccountOrder();
        gatewayAccountOrder.setAccountId(gatewayAccount.getId());
        gatewayAccountOrder.setOrderTraceNo(traceNo);
        gatewayAccountOrder.setStatus(OrderStatusEnum.PAID.getCode());
        gatewayAccountOrder.setPurpose(OrderPurposeTypeEnum.BALANCE_RECHARGE.getCode());
        gatewayAccountOrder.setAmount(amount);
        gatewayAccountOrder.setTrdOrderTraceNo(String.valueOf(traceNo));
        gatewayAccountOrder.setSource(OrderSourceTypeEnum.INSIDE.getCode());
        gatewayAccountOrder.setCreateDate(new Date());
        this.save(gatewayAccountOrder);

        gatewayAccountService.rechargeBalance(amount, appId);
    }
}

