package com.jimmy.friday.framework.schedule;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.schedule.ScheduleInfo;
import com.jimmy.friday.boot.enums.schedule.BlockHandlerStrategyTypeEnum;
import com.jimmy.friday.boot.enums.schedule.ScheduleSourceEnum;
import com.jimmy.friday.boot.exception.ScheduleException;
import com.jimmy.friday.boot.message.schedule.ScheduleAppend;
import com.jimmy.friday.boot.message.schedule.ScheduleDelete;
import com.jimmy.friday.framework.base.Job;
import com.jimmy.friday.framework.core.ConfigLoad;
import com.jimmy.friday.framework.other.CronExpression;
import com.jimmy.friday.framework.other.JobProxy;
import com.jimmy.friday.framework.support.TransmitSupport;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.util.Collection;
import java.util.Map;

public class ScheduleCenter {

    private final Map<String, ScheduleInfo> scheduleInfoMap = Maps.newHashMap();

    private ConfigLoad configLoad;

    private TransmitSupport transmitSupport;

    private DefaultListableBeanFactory beanFactory;

    public ScheduleCenter(ConfigLoad configLoad, TransmitSupport transmitSupport, DefaultListableBeanFactory beanFactory) {
        this.configLoad = configLoad;
        this.beanFactory = beanFactory;
        this.transmitSupport = transmitSupport;
    }

    public ScheduleInfo getScheduleInfo(String scheduleId) {
        return scheduleInfoMap.get(scheduleId);
    }

    public void remove(String scheduleId) {
        String springBeanId = scheduleId + "_Schedule";

        scheduleInfoMap.remove(scheduleId);

        if (beanFactory.containsBeanDefinition(springBeanId)) {
            beanFactory.removeBeanDefinition(springBeanId);
        }

        ScheduleDelete scheduleDelete = new ScheduleDelete();
        scheduleDelete.setScheduleId(scheduleId);
        scheduleDelete.setApplicationName(configLoad.getApplicationName());
        transmitSupport.send(scheduleDelete);
    }

    public void register(String scheduleId, String cron, Job job, BlockHandlerStrategyTypeEnum blockHandlerStrategyType) {
        if (StrUtil.isEmpty(scheduleId)) {
            throw new ScheduleException("定时器唯一标识未定义");
        }

        if (StrUtil.isEmpty(cron)) {
            throw new ScheduleException("cron表达式未定义");
        }

        if (!CronExpression.isValidExpression(cron)) {
            throw new ScheduleException("cron表达式:" + cron + "错误");
        }

        String springBeanId = scheduleId + "_Schedule";

        ScheduleInfo scheduleInfo = new ScheduleInfo();
        scheduleInfo.setScheduleId(scheduleId);
        scheduleInfo.setMethodName("run");
        scheduleInfo.setClassName(JobProxy.class.getTypeName());
        scheduleInfo.setCron(cron);
        scheduleInfo.setSpringBeanId(springBeanId);
        scheduleInfo.setBlockHandlerStrategyType(blockHandlerStrategyType);
        scheduleInfo.setScheduleSource(ScheduleSourceEnum.MANUAL);

        if (scheduleInfoMap.put(scheduleId, scheduleInfo) != null) {
            throw new ScheduleException(scheduleId + "定时器重复定义");
        }

        if (!beanFactory.containsBeanDefinition(springBeanId)) {
            BeanDefinitionBuilder bd = BeanDefinitionBuilder.genericBeanDefinition(JobProxy.class);
            bd.addPropertyValue("job", job);
            beanFactory.registerBeanDefinition(springBeanId, bd.getRawBeanDefinition());
            this.beanFactory.getBean(springBeanId);
        }

        ScheduleAppend scheduleAppend = new ScheduleAppend();
        scheduleAppend.setScheduleInfo(scheduleInfo);
        scheduleAppend.setApplicationName(configLoad.getApplicationName());
        transmitSupport.send(scheduleAppend);
    }

    public void register(String className, String methodName, String scheduleId, String cron, BlockHandlerStrategyTypeEnum blockHandlerStrategyType) {
        if (StrUtil.isEmpty(cron)) {
            throw new ScheduleException("cron表达式未定义");
        }

        if (StrUtil.isEmpty(scheduleId)) {
            throw new ScheduleException("定时器唯一标识未定义");
        }

        if (!CronExpression.isValidExpression(cron)) {
            throw new ScheduleException("cron表达式:" + cron + "错误");
        }

        ScheduleInfo scheduleInfo = new ScheduleInfo();
        scheduleInfo.setScheduleId(scheduleId);
        scheduleInfo.setMethodName(methodName);
        scheduleInfo.setClassName(className);
        scheduleInfo.setCron(cron);
        scheduleInfo.setBlockHandlerStrategyType(blockHandlerStrategyType);
        scheduleInfo.setScheduleSource(ScheduleSourceEnum.ANNOTATION);

        if (scheduleInfoMap.put(scheduleId, scheduleInfo) != null) {
            throw new ScheduleException(scheduleId + "定时器重复定义");
        }
    }

    public void setSpringBeanId(String scheduleId, String springBeanId) {
        ScheduleInfo scheduleInfo = this.getScheduleInfo(scheduleId);
        if (scheduleInfo == null) {
            throw new ScheduleException(scheduleId + "定时器不存在");
        }

        scheduleInfo.setSpringBeanId(springBeanId);
    }

    public Collection<ScheduleInfo> getSchedules() {
        return scheduleInfoMap.values();
    }
}
