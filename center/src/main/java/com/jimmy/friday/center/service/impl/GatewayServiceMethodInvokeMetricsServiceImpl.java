package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.InvokeMetricsTypeEnum;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.GatewayServiceMethodInvokeMetricsDao;
import com.jimmy.friday.center.entity.GatewayServiceMethodInvokeMetrics;
import com.jimmy.friday.center.service.GatewayServiceMethodInvokeMetricsService;
import com.jimmy.friday.center.service.GatewayServiceMethodService;
import com.jimmy.friday.center.utils.RedisConstants;
import com.jimmy.friday.center.vo.MetricsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * (GatewayServiceMethodInvokeMetrics)表服务实现类
 *
 * @author makejava
 * @since 2024-03-26 16:49:31
 */
@Service("gatewayServiceMethodInvokeMetricsService")
public class GatewayServiceMethodInvokeMetricsServiceImpl extends ServiceImpl<GatewayServiceMethodInvokeMetricsDao, GatewayServiceMethodInvokeMetrics> implements GatewayServiceMethodInvokeMetricsService, Initialize {

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayServiceMethodService gatewayServiceMethodService;

    @Override
    public List<MetricsVO> getMetrics(Long methodId, InvokeMetricsTypeEnum invokeMetricsTypeEnum) {
        QueryWrapper<GatewayServiceMethodInvokeMetrics> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("method_id", methodId);
        queryWrapper.eq("meter_unit", invokeMetricsTypeEnum.getCode());
        List<GatewayServiceMethodInvokeMetrics> list = this.list(queryWrapper);

        Date first = DateUtil.offsetDay(new Date(), -7);
        Map<String, Integer> collect = CollUtil.isEmpty(list) ? Maps.newHashMap() : list.stream().collect(Collectors.toMap(GatewayServiceMethodInvokeMetrics::getMeterDate, GatewayServiceMethodInvokeMetrics::getInvokeCount));

        MetricsVO metricsVO = new MetricsVO();
        metricsVO.setMeterDate(DateUtil.format(first, DatePattern.NORM_DATE_PATTERN));
        metricsVO.setInvokeCount(MapUtil.getInt(collect, metricsVO.getMeterDate(), 0));

        List<MetricsVO> result = Lists.newArrayList(metricsVO);
        for (int i = 1; i < 7; i++) {
            DateTime dateTime = DateUtil.offsetDay(first, i);

            MetricsVO vo = new MetricsVO();
            vo.setMeterDate(DateUtil.format(dateTime, DatePattern.NORM_DATE_PATTERN));
            vo.setInvokeCount(MapUtil.getInt(collect, vo.getMeterDate(), 0));
            result.add(vo);
        }

        return result;
    }

    @Override
    public void init() throws Exception {
        CronUtil.schedule(IdUtil.simpleUUID(), "0 0 23 * * ?", () -> {
            Date now = new Date();
            String today = DateUtil.today();
            List<GatewayServiceMethodInvokeMetrics> list = Lists.newArrayList();
            //获取method和service映射
            Map<Long, Long> methodIdMapperServiceId = gatewayServiceMethodService.getMethodIdMapperServiceId();
            //删除7天前的数据
            QueryWrapper<GatewayServiceMethodInvokeMetrics> queryWrapper = new QueryWrapper<>();
            queryWrapper.le("create_date", DateUtil.offsetDay(now, -7));
            this.remove(queryWrapper);

            Iterable<String> todayInvokeKeys = attachmentCache.keys(RedisConstants.Gateway.GATEWAY_METHOD_TODAY_INVOKE_COUNT + "*");
            if (CollUtil.isNotEmpty(todayInvokeKeys)) {
                for (String key : todayInvokeKeys) {
                    String attachment = attachmentCache.attachment(key);
                    if (StrUtil.isNotEmpty(attachment)) {
                        attachmentCache.remove(key);

                        Long methodId = Convert.toLong(StrUtil.removeAll(key, RedisConstants.Gateway.GATEWAY_METHOD_TODAY_INVOKE_COUNT));
                        if (methodId == null || !methodIdMapperServiceId.containsKey(methodId)) {
                            continue;
                        }

                        GatewayServiceMethodInvokeMetrics gatewayServiceMethodInvokeMetrics = new GatewayServiceMethodInvokeMetrics();
                        gatewayServiceMethodInvokeMetrics.setServiceId(methodIdMapperServiceId.get(methodId));
                        gatewayServiceMethodInvokeMetrics.setMethodId(methodId);
                        gatewayServiceMethodInvokeMetrics.setInvokeCount(Convert.toInt(attachment, 0));
                        gatewayServiceMethodInvokeMetrics.setMeterUnit(InvokeMetricsTypeEnum.EVERYDAY.getCode());
                        gatewayServiceMethodInvokeMetrics.setMeterDate(today);
                        gatewayServiceMethodInvokeMetrics.setCreateDate(now);
                        list.add(gatewayServiceMethodInvokeMetrics);
                    }
                }
            }

            Iterable<String> historyKeys = attachmentCache.keys(RedisConstants.Gateway.GATEWAY_METHOD_HISTORY_INVOKE_COUNT + "*");
            if (CollUtil.isNotEmpty(historyKeys)) {
                for (String key : historyKeys) {
                    String attachment = attachmentCache.attachment(key);
                    if (StrUtil.isNotEmpty(attachment)) {
                        Long methodId = Convert.toLong(StrUtil.removeAll(key, RedisConstants.Gateway.GATEWAY_METHOD_HISTORY_INVOKE_COUNT));
                        if (methodId == null || !methodIdMapperServiceId.containsKey(methodId)) {
                            attachmentCache.remove(key);
                            continue;
                        }

                        GatewayServiceMethodInvokeMetrics gatewayServiceMethodInvokeMetrics = new GatewayServiceMethodInvokeMetrics();
                        gatewayServiceMethodInvokeMetrics.setServiceId(methodIdMapperServiceId.get(methodId));
                        gatewayServiceMethodInvokeMetrics.setMethodId(methodId);
                        gatewayServiceMethodInvokeMetrics.setInvokeCount(Convert.toInt(attachment, 0));
                        gatewayServiceMethodInvokeMetrics.setMeterUnit(InvokeMetricsTypeEnum.HISTORY.getCode());
                        gatewayServiceMethodInvokeMetrics.setMeterDate(today);
                        gatewayServiceMethodInvokeMetrics.setCreateDate(now);
                        list.add(gatewayServiceMethodInvokeMetrics);
                    }
                }
            }

            if (CollUtil.isNotEmpty(list)) {
                this.saveBatch(list);
            }
        });
    }

    @Override
    public int sort() {
        return 0;
    }
}

