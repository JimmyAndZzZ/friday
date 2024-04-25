package com.jimmy.friday.center.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayAccountDao;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * (GatewayAccount)表服务实现类
 *
 * @author makejava
 * @since 2023-12-08 14:17:22
 */
@Service("gatewayAccountService")
public class GatewayAccountServiceImpl extends ServiceImpl<GatewayAccountDao, GatewayAccount> implements GatewayAccountService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayAccountDao gatewayAccountDao;

    @Override
    public GatewayAccount register(String name) {
        GatewayAccount gatewayAccount = new GatewayAccount();
        gatewayAccount.setBalance(new BigDecimal(0));
        gatewayAccount.setLvl(0);
        gatewayAccount.setStatus(YesOrNoEnum.YES.getCode());
        gatewayAccount.setTitle(name);
        gatewayAccount.setCreateDate(new Date());
        gatewayAccount.setUid(ShortUUID.uuid());
        gatewayAccount.setSeckey(IdUtil.simpleUUID());
        gatewayAccount.setBalance(new BigDecimal(0));
        this.save(gatewayAccount);
        return gatewayAccount;
    }

    @Override
    public GatewayAccount queryByAppId(String appId) {
        return attachmentCache.attachment(RedisConstants.Gateway.GATEWAY_ACCOUNT_CACHE, appId, GatewayAccount.class, new Obtain<GatewayAccount>() {
            @Override
            public GatewayAccount obtain() {
                QueryWrapper<GatewayAccount> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("uid", appId);
                return getOne(queryWrapper);
            }
        });
    }

    @Override
    public String getAppIdById(Long id) {
        return attachmentCache.attachment(RedisConstants.Gateway.GATEWAY_ACCOUNT_APP_ID_CACHE, id.toString(), String.class, () -> {
            GatewayAccount byId = GatewayAccountServiceImpl.super.getById(id);
            return byId != null ? byId.getUid() : null;
        });
    }

    @Override
    public boolean deductBalance(BigDecimal cost, String uid) {
        boolean b = gatewayAccountDao.deductBalance(cost, uid);

        if (b && cost.compareTo(new BigDecimal(0)) > 0) {
            Date now = new Date();
            BigDecimal multiply = cost.multiply(new BigDecimal(1000));

            attachmentCache.increment(RedisConstants.Gateway.TODAY_COST_AMOUNT + uid, multiply.longValue());
            attachmentCache.expire(RedisConstants.Gateway.TODAY_COST_AMOUNT + uid, DateUtil.between(now, DateUtil.endOfDay(now), DateUnit.SECOND), TimeUnit.SECONDS);
            attachmentCache.remove(RedisConstants.Gateway.GATEWAY_ACCOUNT_CACHE, uid);
        }

        return b;
    }

    @Override
    public boolean rechargeBalance(BigDecimal cost, String uid) {
        attachmentCache.remove(RedisConstants.Gateway.GATEWAY_ACCOUNT_CACHE, uid);
        return gatewayAccountDao.rechargeBalance(cost, uid);
    }

    @Override
    public void rollbackBalance(BigDecimal cost, String uid) {
        boolean b = this.rechargeBalance(cost, uid);
        if (b && cost.compareTo(new BigDecimal(0)) > 0) {
            Date now = new Date();
            BigDecimal multiply = cost.multiply(new BigDecimal(1000));

            attachmentCache.decrement(RedisConstants.Gateway.TODAY_COST_AMOUNT + uid, multiply.longValue());
            attachmentCache.expire(RedisConstants.Gateway.TODAY_COST_AMOUNT + uid, DateUtil.between(now, DateUtil.endOfDay(now), DateUnit.SECOND), TimeUnit.SECONDS);
            attachmentCache.remove(RedisConstants.Gateway.GATEWAY_ACCOUNT_CACHE, uid);
        }
    }

    @Override
    public BigDecimal getTodayCostAmount(String uid) {
        String s = attachmentCache.attachment(RedisConstants.Gateway.TODAY_COST_AMOUNT + uid);
        return StrUtil.isEmpty(s) ? new BigDecimal(0) : new BigDecimal(s).divide(new BigDecimal(1000), 2, RoundingMode.HALF_DOWN);
    }
}

