package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.ServiceStatusEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceProviderDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.service.GatewayServiceProviderService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * (GatewayServiceProvider)表服务实现类
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Slf4j
@Service("gatewayServiceProviderService")
public class GatewayServiceProviderServiceImpl extends ServiceImpl<GatewayServiceProviderDao, GatewayServiceProvider> implements GatewayServiceProviderService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayServiceProviderDao gatewayServiceProviderDao;

    @Override
    public boolean updateStatus(String status, Long id) {
        return gatewayServiceProviderDao.updateStatus(status, id);
    }

    @Override
    public List<GatewayServiceProvider> queryByServiceId(Long serviceId) {
        QueryWrapper<GatewayServiceProvider> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_id", serviceId);
        queryWrapper.eq("status", ServiceStatusEnum.ALIVE.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public void update(com.jimmy.friday.boot.core.gateway.Service service, Boolean isManual) {
        String name = service.getName();
        Integer port = service.getPort();
        String ipAddress = service.getIpAddress();

        GatewayService gatewayService = gatewayServiceService.getGatewayService(service);

        GatewayServiceProvider provider = this.query(name, port, ipAddress, gatewayService.getId());
        if (provider == null) {
            throw new GatewayException("目标服务不存在");
        }

        provider.setCreateDate(new Date());

        if (isManual) {
            provider.setWeight(service.getWeight());
            provider.setIsManual(YesOrNoEnum.YES.getCode());
        } else {
            provider.setStatus(service.getStatus().getCode());
        }

        this.updateById(provider);
    }

    @Override
    public void register(GatewayService gatewayService, com.jimmy.friday.boot.core.gateway.Service service) {
        String name = service.getName();
        Integer port = service.getPort();
        String ipAddress = service.getIpAddress();

        Long serviceId = gatewayService.getId();

        GatewayServiceProvider provider = this.query(name, port, ipAddress, gatewayService.getId());
        if (provider == null) {
            provider = new GatewayServiceProvider();
            provider.setServiceId(serviceId);
            provider.setPort(port);
            provider.setIpAddress(ipAddress);
            provider.setStatus(service.getStatus().getCode());
            provider.setWeight(service.getWeight());
            provider.setIsManual(YesOrNoEnum.NO.getCode());
            provider.setCreateDate(new Date());
            this.save(provider);
        } else {
            provider.setStatus(service.getStatus().getCode());
            provider.setCreateDate(new Date());

            if (YesOrNoEnum.YES.getCode().equals(provider.getIsManual())) {
                Integer weight = provider.getWeight();
                if (weight != null) {
                    service.setWeight(weight);
                }
            } else {
                provider.setWeight(service.getWeight());
            }

            this.updateById(provider);
        }
    }

    @Override
    public GatewayServiceProvider query(com.jimmy.friday.boot.core.gateway.Service service) {
        return this.query(service.getName(), service.getPort(), service.getIpAddress(), gatewayServiceService.getGatewayService(service).getId());
    }

    /**
     * 获取provider
     *
     * @param name
     * @param port
     * @param ipAddress
     * @param serviceId
     * @return
     */
    private GatewayServiceProvider query(String name, Integer port, String ipAddress, Long serviceId) {
        String key = StrUtil.builder().append(name).append(":").append(ipAddress).append(":").append(port).toString();

        return attachmentCache.attachment(RedisConstants.Gateway.SERVICE_PROVIDER_CACHE, key, GatewayServiceProvider.class, () -> {
            QueryWrapper<GatewayServiceProvider> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("port", port);
            queryWrapper.eq("ip_address", ipAddress);
            queryWrapper.eq("service_id", serviceId);
            return getOne(queryWrapper);
        });
    }
}

