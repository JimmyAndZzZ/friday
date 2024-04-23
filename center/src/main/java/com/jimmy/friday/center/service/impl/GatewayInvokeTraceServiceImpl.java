package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.entity.GatewayInvokeTrace;
import com.jimmy.friday.center.dao.GatewayInvokeTraceDao;
import com.jimmy.friday.center.service.GatewayInvokeTraceService;
import org.springframework.stereotype.Service;

@Service("gatewayInvokeTraceService")
public class GatewayInvokeTraceServiceImpl extends ServiceImpl<GatewayInvokeTraceDao, GatewayInvokeTrace> implements GatewayInvokeTraceService {


    @Override
    public IPage<GatewayInvokeTrace> page(Long serviceId,
                                             Long providerId,
                                             Long methodId,
                                             String startDate,
                                             String endDate,
                                             String appId,
                                             Boolean isSuccess,
                                             Integer pageNo,
                                             Integer pageSize) {

        QueryWrapper<GatewayInvokeTrace> queryWrapper = new QueryWrapper<>();

        if (serviceId != null) {
            queryWrapper.eq("service_id", serviceId);
        }

        if (providerId != null) {
            queryWrapper.eq("provider_id", providerId);
        }

        if (methodId != null) {
            queryWrapper.eq("method_id", methodId);
        }

        if (StrUtil.isNotEmpty(startDate)) {
            queryWrapper.ge("create_time", startDate + " 00:00:00");
        }

        if (StrUtil.isNotEmpty(endDate)) {
            queryWrapper.le("create_time", endDate + " 23:59:59");
        }

        if (StrUtil.isNotEmpty(appId)) {
            queryWrapper.eq("app_id", appId);
        }

        if (isSuccess != null) {
            queryWrapper.eq("is_success", isSuccess ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
        }

        queryWrapper.orderByDesc("create_time");

        return this.page(new Page<>(pageNo, pageSize), queryWrapper);
    }
}
