package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.center.entity.GatewayAccountInvokeCount;
import com.jimmy.friday.center.vo.InvokeCountVO;

import java.util.List;

/**
 * (GatewayAccountInvokeCount)表服务接口
 *
 * @author makejava
 * @since 2024-01-09 14:52:36
 */
public interface GatewayAccountInvokeCountService extends IService<GatewayAccountInvokeCount> {

    List<InvokeCountVO> getMonthInvokeCount(String appId);
}

