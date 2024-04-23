package com.jimmy.friday.center.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.ServiceWarnTypeEnum;
import com.jimmy.friday.center.config.FridayConfigProperties;
import com.jimmy.friday.center.dao.GatewayServiceWarnDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import com.jimmy.friday.center.entity.GatewayServiceWarn;
import com.jimmy.friday.center.event.ServiceWarnEvent;
import com.jimmy.friday.center.service.GatewayServiceProviderService;
import com.jimmy.friday.center.service.GatewayServiceService;
import com.jimmy.friday.center.service.GatewayServiceWarnService;
import com.jimmy.friday.center.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private FridayConfigProperties fridayConfigProperties;

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

        String remindUrl = fridayConfigProperties.getRemindUrl();
        if (StrUtil.isNotEmpty(remindUrl)) {
            String title = "服务异常:" + serviceWarnType.getMessage();

            Map<String, Object> body = Maps.newHashMap();
            body.put("title", title);
            body.put("content", content);
            body.put("result", "result:false");
            HttpUtil.post(remindUrl, JsonUtil.toString(body));
        }
    }
}

