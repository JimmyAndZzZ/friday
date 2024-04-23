package com.jimmy.friday.center.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.boot.core.agent.Trace;
import com.jimmy.friday.boot.enums.YesOrNoEnum;
import com.jimmy.friday.boot.other.ShortUUID;
import com.jimmy.friday.center.annotation.Async;
import com.jimmy.friday.center.dao.HawkEyeLogDao;
import com.jimmy.friday.center.entity.HawkEyeLog;
import com.jimmy.friday.center.service.HawkEyeLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * (HawkEyeLog)表服务实现类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Slf4j
@Service("hawkEyeLogService")
public class HawkEyeLogServiceImpl extends ServiceImpl<HawkEyeLogDao, HawkEyeLog> implements HawkEyeLogService {

    @Override
    @Async(topic = "hawk_eye_log_topic", groupId = "hawk_eye_log")
    public void push(List<Trace> traces) {
        try {
            this.saveBatch(traces.stream().map(bean -> {
                HawkEyeLog hawkEyeLog = new HawkEyeLog();
                hawkEyeLog.setIsLog(bean.getIsLog() ? YesOrNoEnum.YES.getCode() : YesOrNoEnum.NO.getCode());
                hawkEyeLog.setLogMessage(bean.getLogMessage());
                hawkEyeLog.setModuleName(bean.getApplicationName());
                hawkEyeLog.setId(ShortUUID.uuid());
                hawkEyeLog.setClassName(bean.getClassName());
                hawkEyeLog.setCreateDate(bean.getDate());
                hawkEyeLog.setLevel(bean.getLevel());
                hawkEyeLog.setMethodName(bean.getMethodName());
                hawkEyeLog.setModifyDate(bean.getDate());
                hawkEyeLog.setParam(bean.getParam());
                hawkEyeLog.setResult(bean.getResult());
                hawkEyeLog.setSpanId(bean.getSpanId());
                hawkEyeLog.setTraceId(bean.getTraceId());
                return hawkEyeLog;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("日志数据入库失败", e);
        }
    }
}

