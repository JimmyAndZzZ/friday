package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayAccount;

import java.math.BigDecimal;

/**
 * (GatewayAccount)表服务接口
 *
 * @author makejava
 * @since 2023-12-08 14:17:21
 */
public interface GatewayAccountService extends IService<GatewayAccount> {

    String getAppIdById(Long id);

    GatewayAccount register(String name);

    BigDecimal getTodayCostAmount(String uid);

    void rollbackBalance(BigDecimal cost, String uid);

    boolean deductBalance(BigDecimal cost, String uid);

    boolean rechargeBalance(BigDecimal cost, String uid);

    GatewayAccount queryByAppId(String appId);
}

