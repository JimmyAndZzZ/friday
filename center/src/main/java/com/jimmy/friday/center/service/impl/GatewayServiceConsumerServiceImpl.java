package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceConsumerDao;
import com.jimmy.friday.center.entity.GatewayServiceConsumer;
import com.jimmy.friday.center.service.GatewayServiceConsumerService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (GatewayServiceConsumer)表服务实现类
 *
 * @author makejava
 * @since 2024-03-25 15:50:03
 */
@Service("gatewayServiceConsumerService")
public class GatewayServiceConsumerServiceImpl extends ServiceImpl<GatewayServiceConsumerDao, GatewayServiceConsumer> implements GatewayServiceConsumerService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public List<GatewayServiceConsumer> queryByServiceId(Long serviceId) {
        QueryWrapper<GatewayServiceConsumer> wrapper = new QueryWrapper<>();
        wrapper.eq("service_id", serviceId);
        return this.list(wrapper);
    }

    @Override
    public void save(Long serviceId, String ipAddress, String appId, String clientName, Long providerId) {
        if (StrUtil.isEmpty(ipAddress)) {
            return;
        }

        if (StrUtil.isEmpty(appId)) {
            return;
        }

        if (StrUtil.isEmpty(clientName)) {
            return;
        }

        if (serviceId == null) {
            return;
        }

        if (providerId == null) {
            return;
        }

        String key = SecureUtil.md5(serviceId + ":" + ipAddress + ":" + appId + ":" + clientName);

        if (attachmentCache.setIfAbsent(RedisConstants.Gateway.GATEWAY_SERVICE_CONSUMER + key, YesOrNoEnum.YES.getCode(), 30L, TimeUnit.MINUTES)) {
            GatewayServiceConsumer query = this.query(serviceId, ipAddress, appId, clientName);

            if (query != null) {
                attachmentCache.expire(RedisConstants.Gateway.GATEWAY_SERVICE_CONSUMER + key, 24L, TimeUnit.HOURS);

                query.setProviderId(providerId);
                this.updateById(query);
            } else {
                GatewayServiceConsumer gatewayServiceConsumer = new GatewayServiceConsumer();
                gatewayServiceConsumer.setServiceId(serviceId);
                gatewayServiceConsumer.setClientName(clientName);
                gatewayServiceConsumer.setAppId(appId);
                gatewayServiceConsumer.setIpAddress(ipAddress);
                gatewayServiceConsumer.setCreateDate(new Date());
                gatewayServiceConsumer.setProviderId(providerId);
                this.save(gatewayServiceConsumer);
            }
        } else {
            GatewayServiceConsumer query = this.query(serviceId, ipAddress, appId, clientName);

            if (query != null) {
                query.setProviderId(providerId);
                this.updateById(query);
            }
        }
    }

    @Override
    public GatewayServiceConsumer query(Long serviceId, String ipAddress, String appId, String clientName) {
        QueryWrapper<GatewayServiceConsumer> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_id", serviceId);
        queryWrapper.eq("ip_address", ipAddress);
        queryWrapper.eq("app_id", appId);
        queryWrapper.eq("client_name", clientName);
        queryWrapper.last(" limit 0,1");
        return this.getOne(queryWrapper);
    }
}

