package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.enums.schedule.ScheduleExecutorStatusEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.ScheduleExecutorDao;
import com.jimmy.friday.center.entity.ScheduleExecutor;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * (ScheduleExecutor)表服务实现类
 *
 * @author makejava
 * @since 2024-04-28 15:32:14
 */
@Service("scheduleExecutorService")
public class ScheduleExecutorServiceImpl extends ServiceImpl<ScheduleExecutorDao, ScheduleExecutor> implements ScheduleExecutorService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public List<ScheduleExecutor> queryByApplicationName(String applicationName) {
        QueryWrapper<ScheduleExecutor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_name", applicationName);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> getApplicationList() {
        QueryWrapper<ScheduleExecutor> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT application_name as application_name");
        List<ScheduleExecutor> list = this.list(queryWrapper);
        return CollUtil.isNotEmpty(list) ? list.stream().map(ScheduleExecutor::getApplicationName).collect(Collectors.toList()) : Lists.newArrayList();
    }

    @Override
    public ScheduleExecutor getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_EXECUTOR_CACHE, id.toString(), ScheduleExecutor.class, () -> super.getById(id));
    }

    @Override
    public ScheduleExecutor register(String applicationName, String ip) {
        ScheduleExecutor one = this.query(applicationName, ip);
        if (one != null) {
            one.setStatus(ScheduleExecutorStatusEnum.ALIVE.getCode());
            this.updateById(one);
        } else {
            if (Boolean.TRUE.equals(attachmentCache.setIfAbsent(RedisConstants.Schedule.SCHEDULE_EXECUTOR_REGISTER + applicationName + ":" + ip, YesOrNoEnum.YES.getCode(), 60L, TimeUnit.SECONDS))) {
                one = new ScheduleExecutor();
                one.setStatus(ScheduleExecutorStatusEnum.ALIVE.getCode());
                one.setApplicationName(applicationName);
                one.setIpAddress(ip);
                this.save(one);
            }
        }

        return one;
    }

    @Override
    public void offline(String applicationName, String ip) {
        ScheduleExecutor query = this.query(applicationName, ip);
        if (query != null) {
            query.setStatus(ScheduleExecutorStatusEnum.DISCONNECT.getCode());
            this.updateById(query);
        }
    }

    @Override
    public ScheduleExecutor query(String applicationName, String ip) {
        return attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_EXECUTOR_CACHE, applicationName + ":" + ip, ScheduleExecutor.class, new Obtain<ScheduleExecutor>() {
            @Override
            public ScheduleExecutor obtain() {
                QueryWrapper<ScheduleExecutor> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("application_name", applicationName);
                queryWrapper.eq("ip_address", ip);
                return getOne(queryWrapper);
            }
        });
    }
}

