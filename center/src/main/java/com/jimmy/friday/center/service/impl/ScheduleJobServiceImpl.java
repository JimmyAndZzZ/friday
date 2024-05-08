package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.schedule.ScheduleSourceEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleStatusEnum;
import com.jimmy.friday.center.core.AttachmentCache;
import com.jimmy.friday.center.dao.ScheduleJobDao;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.service.ScheduleJobService;
import com.jimmy.friday.center.utils.RedisConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
 * (ScheduleJob)表服务实现类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Service("scheduleJobInfoService")
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobDao, ScheduleJob> implements ScheduleJobService {

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public ScheduleJob getById(Serializable id) {
        return attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_JOB_CACHE, id.toString(), ScheduleJob.class, () -> super.getById(id));
    }

    @Override
    public void updateNextExecuteTime(Long nextTime, Long id) {
        baseMapper.updateNextExecuteTime(nextTime, id);
    }

    @Override
    public ScheduleJob queryByCodeAndApplicationName(String code, String applicationName) {
        QueryWrapper<ScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code", code);
        queryWrapper.eq("application_name", applicationName);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ScheduleJob> queryByApplicationName(String applicationName) {
        QueryWrapper<ScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_name", applicationName);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByApplicationName(String applicationName, ScheduleSourceEnum scheduleSourceEnum) {
        QueryWrapper<ScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("application_name", applicationName);

        if (scheduleSourceEnum != null) {
            queryWrapper.eq("source", scheduleSourceEnum.getCode());
        }

        this.remove(queryWrapper);
    }

    @Override
    public List<ScheduleJob> queryExecuteJobs(Long time, Integer size) {
        QueryWrapper<ScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", ScheduleStatusEnum.OPEN.getCode());
        queryWrapper.le("next_time", time);
        queryWrapper.orderByDesc("id");
        queryWrapper.last(" limit " + size);
        return this.list(queryWrapper);
    }

    @Override
    public void updateExecuteTime(Long lastTime, Long nextTime, Long id) {
        baseMapper.updateExecuteTime(lastTime, nextTime, id);
    }

    @Override
    public void updateStatus(String status, Long id) {
        baseMapper.updateStatus(status, id);
    }

    @Override
    public boolean needExecute(Long id, Long nextTime) {
        QueryWrapper<ScheduleJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("next_time", nextTime);
        queryWrapper.eq("status", ScheduleStatusEnum.OPEN.getCode());
        return this.count(queryWrapper) == 1;
    }
}

