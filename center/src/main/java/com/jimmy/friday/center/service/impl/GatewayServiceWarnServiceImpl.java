package com.jimmy.friday.center.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.gateway.ServiceWarnTypeEnum;
import com.jimmy.friday.center.dao.GatewayServiceWarnDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.entity.GatewayServiceWarn;
import com.jimmy.friday.center.event.ServiceWarnEvent;
import com.jimmy.friday.center.service.GatewayServiceProviderService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.service.GatewayServiceWarnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * (GatewayServiceWarn)表服务实现类
 *
 * @author makejava
 * @since 2024-04-16 14:05:52
 */
@Service("gatewayServiceWarnService")
public class GatewayServiceWarnServiceImpl extends ServiceImpl<GatewayServiceWarnDao, GatewayServiceWarn> implements GatewayServiceWarnService, ApplicationListener<ServiceWarnEvent> {

    @Autowired
    private GatewayServiceService gatewayServiceService;

    @Autowired
    private GatewayServiceProviderService gatewayServiceProviderService;

    @Override
    public List<GatewayServiceWarn> queryList(Long serviceId, Long providerId) {
        QueryWrapper<GatewayServiceWarn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("service_id", serviceId);

        if (providerId != null) {
            queryWrapper.eq("provider_id", providerId);
        }

        queryWrapper.orderByDesc("create_date");
        queryWrapper.last("limit 0,10");
        return this.list(queryWrapper);
    }

    @Override
    public void onApplicationEvent(ServiceWarnEvent event) {
        Long serviceId = event.getServiceId();
        Date createDate = event.getCreateDate();
        Long providerId = event.getProviderId();
        ServiceWarnTypeEnum serviceWarnType = event.getServiceWarnType();

        GatewayService gatewayService = gatewayServiceService.queryById(serviceId);
        if (gatewayService == null) {
            return;
        }

        GatewayServiceProvider byId = gatewayServiceProviderService.getById(providerId);
        if (byId == null) {
            return;
        }

        String content = "服务异常:" + serviceWarnType.getMessage() +
                "\n" + "服务名:" + gatewayService.getApplicationName() +
                "\n" + "版本:" + gatewayService.getVersion() +
                "\n" + "服务类型:" + gatewayService.getType() +
                "\n" + "provider:" + byId.getIpAddress() + ":" + byId.getPort() +
                "\n" + "时间:" + DateUtil.format(createDate, DatePattern.NORM_DATETIME_PATTERN);

        GatewayServiceWarn gatewayServiceWarn = new GatewayServiceWarn();
        gatewayServiceWarn.setServiceId(serviceId);
        gatewayServiceWarn.setCreateDate(createDate);
        gatewayServiceWarn.setType(serviceWarnType.getCode());
        gatewayServiceWarn.setProviderId(providerId);
        gatewayServiceWarn.setMessage(content);
        this.save(gatewayServiceWarn);
    }
}

