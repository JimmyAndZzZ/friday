package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.center.dao.ScheduleJobInfoDao;
import com.jimmy.friday.center.entity.ScheduleJobInfo;
import com.jimmy.friday.center.service.ScheduleJobInfoService;
import org.springframework.stereotype.Service;

/**
 * (ScheduleJobInfo)表服务实现类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Service("scheduleJobInfoService")
public class ScheduleJobInfoServiceImpl extends ServiceImpl<ScheduleJobInfoDao, ScheduleJobInfo> implements ScheduleJobInfoService {

}

