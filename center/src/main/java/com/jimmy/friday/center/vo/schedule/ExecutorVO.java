package com.jimmy.friday.center.vo.schedule;


import com.jimmy.friday.center.entity.ScheduleExecutor;
import lombok.Data;

import java.io.Serializable;


/**
 * (ScheduleExecutor)表实体类
 *
 * @author makejava
 * @since 2024-04-28 15:32:14
 */
@Data
public class ExecutorVO implements Serializable {

    private Long id;

    private String status;

    private String ipAddress;

    private String description;

    public static ExecutorVO build(ScheduleExecutor scheduleExecutor) {
        ExecutorVO executorVO = new ExecutorVO();
        executorVO.setId(scheduleExecutor.getId());
        executorVO.setStatus(scheduleExecutor.getStatus());
        executorVO.setIpAddress(scheduleExecutor.getIpAddress());
        executorVO.setDescription(scheduleExecutor.getDescription());
        return executorVO;
    }
}

