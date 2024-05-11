package com.jimmy.friday.center.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jimmy.friday.boot.enums.schedule.ScheduleRunStatusEnum;
import com.jimmy.friday.center.entity.ScheduleJob;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import com.jimmy.friday.center.service.ScheduleExecutorService;
import com.jimmy.friday.center.service.ScheduleJobLogService;
import com.jimmy.friday.center.service.ScheduleJobService;
import com.jimmy.friday.center.vo.PageInfoVO;
import com.jimmy.friday.center.vo.Result;
import com.jimmy.friday.center.vo.schedule.JobLogVO;
import com.jimmy.friday.center.vo.schedule.JobVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedule")
@Api(tags = "调度API")
@Slf4j
public class ScheduleController {

    @Autowired
    private ScheduleJobService scheduleJobService;

    @Autowired
    private ScheduleJobLogService scheduleJobLogService;

    @Autowired
    private ScheduleExecutorService scheduleExecutorService;

    @GetMapping("/queryLogList")
    @ApiOperation("获取定时器运行日志列表")
    public Result<?> queryLogList(@RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN) Date startDate,
                                  @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = DatePattern.NORM_DATETIME_PATTERN) Date endDate,
                                  @RequestParam(value = "jobId") Long jobId,
                                  @RequestParam(value = "scheduleRunStatus", required = false) ScheduleRunStatusEnum scheduleRunStatus,
                                  @RequestParam(value = "pageNo") Integer pageNo,
                                  @RequestParam(value = "pageSize") Integer pageSize) {
        IPage<ScheduleJobLog> page = scheduleJobLogService.page(startDate, endDate, jobId, scheduleRunStatus, pageNo, pageSize);


        PageInfoVO<JobLogVO> pageInfoVO = new PageInfoVO<>();
        pageInfoVO.setSize(page.getSize());
        pageInfoVO.setTotal(page.getTotal());
        pageInfoVO.setCurrent(page.getCurrent());

        List<ScheduleJobLog> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            pageInfoVO.setRecords(records.stream().map(bean -> {
                JobLogVO jobLogVO = new JobLogVO();
                jobLogVO.setId(bean.getId());
                jobLogVO.setJobId(bean.getJobId());
                jobLogVO.setTraceId(bean.getTraceId());
                jobLogVO.setExecutorId(bean.getExecutorId());
                jobLogVO.setRunStatus(bean.getRunStatus());
                jobLogVO.setErrorMessage(bean.getErrorMessage());
                jobLogVO.setRunParam(bean.getRunParam());

                Long timeoutDate = bean.getTimeoutDate();
                Long jobStartDate = bean.getStartDate();
                Long jobEndDate = bean.getEndDate();

                if (timeoutDate != null && timeoutDate > 0) {
                    jobLogVO.setTimeoutDate(new Date(timeoutDate));
                }

                if (jobStartDate != null && jobStartDate > 0) {
                    jobLogVO.setStartDate(new Date(jobStartDate));
                }

                if (jobEndDate != null && jobEndDate > 0) {
                    jobLogVO.setEndDate(new Date(jobEndDate));
                }

                return jobLogVO;
            }).collect(Collectors.toList()));
        }

        return Result.ok(pageInfoVO);
    }

    @GetMapping("/getApplicationList")
    @ApiOperation("获取应用列表")
    public Result<?> getApplicationList() {
        return Result.ok(scheduleExecutorService.getApplicationList());
    }

    @GetMapping("/queryJobList")
    @ApiOperation("获取定时器列表")
    public Result<?> queryJobList(@RequestParam("applicationName") String applicationName, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        IPage<ScheduleJob> page = scheduleJobService.page(applicationName, pageNo, pageSize);

        PageInfoVO<JobVO> pageInfoVO = new PageInfoVO<>();
        pageInfoVO.setSize(page.getSize());
        pageInfoVO.setTotal(page.getTotal());
        pageInfoVO.setCurrent(page.getCurrent());

        List<ScheduleJob> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            pageInfoVO.setRecords(records.stream().map(bean -> {
                JobVO jobVO = new JobVO();
                jobVO.setId(bean.getId());
                jobVO.setCron(bean.getCron());
                jobVO.setDescription(bean.getDescription());
                jobVO.setCreateDate(bean.getCreateDate());
                jobVO.setUpdateDate(bean.getUpdateDate());
                jobVO.setCode(bean.getCode());
                jobVO.setRunParam(bean.getRunParam());
                jobVO.setBlockStrategy(bean.getBlockStrategy());
                jobVO.setTimeout(bean.getTimeout());
                jobVO.setRetryCount(bean.getRetryCount());
                jobVO.setStatus(bean.getStatus());
                jobVO.setApplicationName(bean.getApplicationName());
                jobVO.setIsManual(bean.getIsManual());
                jobVO.setSource(bean.getSource());

                Long lastTime = bean.getLastTime();
                Long nextTime = bean.getNextTime();

                if (lastTime != null && lastTime > 0) {
                    jobVO.setLastTime(new Date(lastTime));
                }

                if (nextTime != null && nextTime > 0) {
                    jobVO.setNextTime(new Date(nextTime));
                }

                return jobVO;
            }).collect(Collectors.toList()));
        }

        return Result.ok(scheduleExecutorService.getApplicationList());
    }
}
