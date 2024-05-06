package com.jimmy.friday.center.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.ScheduleExecutorStatusEnum;
import com.jimmy.friday.boot.enums.ScheduleStatusEnum;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.center.base.Obtain;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.ScheduleExecutorDao;
import com.jimmy.friday.center.entity.GatewayService;
import com.jimmy.friday.center.entity.ScheduleExecutor;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import com.jimmy.friday.center.utils.RedisConstants;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    public ScheduleExecutor getById(Serializable id){
        return attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_EXECUTOR_CACHE, id.toString(), ScheduleExecutor.class, () -> super.getById(id));
    }

    @Override
    public void register(String applicationName, String ip) {
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

