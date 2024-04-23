package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.enums.InvokeMetricsTypeEnum;
import com.jimmy.friday.center.entity.GatewayServiceMethodInvokeMetrics;
import com.jimmy.friday.center.vo.MetricsVO;

import java.util.List;

/**
 * (GatewayServiceMethodInvokeMetrics)表服务接口
 *
 * @author makejava
 * @since 2024-03-26 16:49:31
 */
public interface GatewayServiceMethodInvokeMetricsService extends IService<GatewayServiceMethodInvokeMetrics> {

    List<MetricsVO> getMetrics(Long methodId, InvokeMetricsTypeEnum invokeMetricsTypeEnum);
}

