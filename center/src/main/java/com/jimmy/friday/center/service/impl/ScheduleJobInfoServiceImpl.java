package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.enums.ScheduleStatusEnum;
import com.jimmy.friday.center.dao.ScheduleJobInfoDao;
import com.jimmy.friday.center.entity.ScheduleJobInfo;
import com.jimmy.friday.center.service.ScheduleJobInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * (ScheduleJobInfo)表服务实现类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Service("scheduleJobInfoService")
public class ScheduleJobInfoServiceImpl extends ServiceImpl<ScheduleJobInfoDao, ScheduleJobInfo> implements ScheduleJobInfoService {

    @Override
    public List<ScheduleJobInfo> queryExecuteJobs(Long time, Integer size) {
        QueryWrapper<ScheduleJobInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", ScheduleStatusEnum.OPEN.getCode());
        queryWrapper.le("next_time", time);
        queryWrapper.orderByDesc("id");
        queryWrapper.last(" limit " + size);
        return this.list(queryWrapper);
    }

    @Override
    public void updateExecuteTime(Long lastTime, Long nextTime, Integer id) {
        baseMapper.updateExecuteTime(lastTime, nextTime, id);
    }

    @Override
    public void updateStatus(String status, Integer id) {
        baseMapper.updateStatus(status, id);
    }

    @Override
    public boolean needExecute(Integer id, Long nextTime) {
        QueryWrapper<ScheduleJobInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("next_time", nextTime);
        queryWrapper.eq("status", ScheduleStatusEnum.OPEN.getCode());
        return this.count(queryWrapper) == 1;
    }

}

