package com.jimmy.friday.center.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * (ScheduleJob)表服务实现类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Service("scheduleJobInfoService")
public class ScheduleJobServiceImpl extends ServiceImpl<ScheduleJobDao, ScheduleJob> implements ScheduleJobService {

    private static final String DEFAULT_ID = "-1";

    @Autowired
    private AttachmentCache attachmentCache;

    @Override
    public boolean updateBlockHandlerStrategyType(Long id, String expect, String update) {
        return baseMapper.updateBlockHandlerStrategyType(id, expect, update);
    }

    @Override
    public ScheduleJob save(ScheduleInfo scheduleInfo, String applicationName) {
        String scheduleId = scheduleInfo.getScheduleId();
        BlockHandlerStrategyTypeEnum blockHandlerStrategyType = scheduleInfo.getBlockHandlerStrategyType();

        ScheduleJob scheduleJob = this.queryByCodeAndApplicationName(scheduleId, applicationName);
        if (scheduleJob != null) {
            attachmentCache.attachString(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleId, scheduleJob.getId().toString());
            return scheduleJob;
        }

        if (attachmentCache.putIfAbsent(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleId, DEFAULT_ID)) {
            try {
                scheduleJob = this.queryByCodeAndApplicationName(scheduleId, applicationName);
                if (scheduleJob != null) {
                    attachmentCache.attachString(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleId, scheduleJob.getId().toString());
                    return scheduleJob;
                } else {
                    scheduleJob = new ScheduleJob();
                    scheduleJob.setTimeout(0L);
                    scheduleJob.setCron(scheduleInfo.getCron());
                    scheduleJob.setRetryCount(0);
                    scheduleJob.setCreateDate(new Date());
                    scheduleJob.setUpdateDate(new Date());
                    scheduleJob.setCode(scheduleId);
                    scheduleJob.setApplicationName(applicationName);
                    scheduleJob.setIsManual(YesOrNoEnum.NO.getCode());
                    scheduleJob.setStatus(ScheduleStatusEnum.OPEN.getCode());
                    scheduleJob.setBlockStrategy(blockHandlerStrategyType.getCode());
                    scheduleJob.setSource(scheduleInfo.getScheduleSource().getCode());
                    this.save(scheduleJob);
                    return scheduleJob;
                }
            } catch (Exception e) {
                attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleId);
                throw e;
            }
        } else {
            Object attachment = attachmentCache.attachment(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleId);
            if (attachment == null || DEFAULT_ID.equals(attachment.toString())) {
                return null;
            }

            return super.getById(Convert.toLong(attachment, 0L));
        }
    }

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

        List<ScheduleJob> list = this.list(queryWrapper);
        if (CollUtil.isNotEmpty(list)) {
            for (ScheduleJob scheduleJob : list) {
                attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleJob.getCode());
                attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CACHE, scheduleJob.getId().toString());
            }

            super.removeByIds(list.stream().map(ScheduleJob::getId).collect(Collectors.toSet()));
        }
    }

    @Override
    public void removeByCodeAndApplicationName(String code, String applicationName) {
        ScheduleJob scheduleJob = this.queryByCodeAndApplicationName(code, applicationName);
        if (scheduleJob != null) {
            attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + applicationName, scheduleJob.getCode());
            attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CACHE, scheduleJob.getId().toString());
            super.removeById(scheduleJob.getId());
        }
    }

    @Override
    public boolean removeById(Serializable id) {
        ScheduleJob byId = super.getById(id);
        if (byId != null) {
            attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + byId.getApplicationName(), byId.getCode());
            attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CACHE, byId.getId().toString());

            return super.removeById(id);
        }

        return false;
    }

    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        Collection<ScheduleJob> scheduleJobs = super.listByIds(idList);
        if (CollUtil.isNotEmpty(scheduleJobs)) {
            for (ScheduleJob scheduleJob : scheduleJobs) {
                attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CODE_ID_MAPPER + scheduleJob.getApplicationName(), scheduleJob.getCode());
                attachmentCache.remove(RedisConstants.Schedule.SCHEDULE_JOB_CACHE, scheduleJob.getId().toString());
            }

            return super.removeByIds(idList);
        }

        return false;
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

