package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.core.StripedLock;
import com.jimmy.friday.center.dao.GatewayAccountInvokeCountDao;
import com.jimmy.friday.center.entity.GatewayAccount;
import com.jimmy.friday.center.entity.GatewayAccountInvokeCount;
import com.jimmy.friday.center.service.GatewayAccountInvokeCountService;
import com.jimmy.friday.center.service.GatewayAccountService;
import com.jimmy.friday.center.utils.RedisConstants;
import com.jimmy.friday.center.vo.gateway.InvokeCountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (GatewayAccountInvokeCount)表服务实现类
 *
 * @author makejava
 * @since 2024-01-09 14:52:36
 */
@Service("gatewayAccountInvokeCountService")
public class GatewayAccountInvokeCountServiceImpl extends ServiceImpl<GatewayAccountInvokeCountDao, GatewayAccountInvokeCount> implements GatewayAccountInvokeCountService, Initialize {

    @Autowired
    private StripedLock stripedLock;

    @Autowired
    private AttachmentCache attachmentCache;

    @Autowired
    private GatewayAccountService gatewayAccountService;

    @Override
    public List<InvokeCountVO> getMonthInvokeCount(String appId) {
        GatewayAccount gatewayAccount = gatewayAccountService.queryByAppId(appId);
        if (gatewayAccount == null) {
            return Lists.newArrayList();
        }

        Date now = new Date();
        String today = attachmentCache.attachment(RedisConstants.Gateway.TODAY_INVOKE_COUNT + appId);

        QueryWrapper<GatewayAccountInvokeCount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", gatewayAccount.getId());
        queryWrapper.ge("invoke_date", Integer.parseInt(DateUtil.format(DateUtil.offsetDay(now, -30), "yyyyMMdd")));
        queryWrapper.le("invoke_date", Integer.parseInt(DateUtil.format(now, "yyyyMMdd")));
        List<GatewayAccountInvokeCount> list = this.list(queryWrapper);

        Map<Integer, Integer> map = CollUtil.isEmpty(list) ? Maps.newHashMap() : list.stream().collect(Collectors.toMap(GatewayAccountInvokeCount::getInvokeDate, GatewayAccountInvokeCount::getInvokeCount));

        List<InvokeCountVO> result = Lists.newArrayList();
        for (int i = 1; i < 30; i++) {
            int yyyyMMdd = Integer.parseInt(DateUtil.format(DateUtil.offsetDay(now, -1 * i), "yyyyMMdd"));

            Integer count = map.get(yyyyMMdd);

            InvokeCountVO vo = new InvokeCountVO();
            vo.setInvokeCount(count != null ? count : 0);
            vo.setInvokeDate(yyyyMMdd);
            result.add(vo);
        }

        InvokeCountVO vo = new InvokeCountVO();
        vo.setInvokeCount(StrUtil.isEmpty(today) ? 0 : Convert.toInt(today, 0));
        vo.setInvokeDate(Integer.parseInt(DateUtil.format(now, "yyyyMMdd")));
        result.add(vo);

        result.sort(Comparator.comparingInt(InvokeCountVO::getInvokeCount));
        return result;
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        CronUtil.schedule(IdUtil.simpleUUID(), "0 0 23 * * ?", () -> stripedLock.tryLock(RedisConstants.Gateway.GATEWAY_ACCOUNT_INVOKE_COUNT_JOB_LOCK, 300L, TimeUnit.SECONDS, () -> {

            int intDate = Integer.parseInt(DateUtil.format(new Date(), "yyyyMMdd"));

            List<GatewayAccountInvokeCount> save = Lists.newArrayList();

            List<GatewayAccount> list = gatewayAccountService.list();
            for (GatewayAccount gatewayAccount : list) {
                String s = attachmentCache.attachment(RedisConstants.Gateway.TODAY_INVOKE_COUNT + gatewayAccount.getAppId());
                if (StrUtil.isNotEmpty(s)) {
                    attachmentCache.remove(RedisConstants.Gateway.TODAY_INVOKE_COUNT + gatewayAccount.getAppId());

                    Integer anInt = Convert.toInt(s);
                    if (anInt != null) {
                        GatewayAccountInvokeCount gatewayAccountInvokeCount = new GatewayAccountInvokeCount();
                        gatewayAccountInvokeCount.setAccountId(gatewayAccount.getId());
                        gatewayAccountInvokeCount.setInvokeDate(intDate);
                        gatewayAccountInvokeCount.setInvokeCount(anInt);
                        save.add(gatewayAccountInvokeCount);
                    }
                }
            }

            if (CollUtil.isNotEmpty(save)) {
                this.saveBatch(save);
            }
        }));
    }

    @Override
    public int sort() {
        return 0;
    }
}

