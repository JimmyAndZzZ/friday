package com.jimmy.friday.center.vo.schedule;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * (ScheduleJobLog)表实体类
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
@Data
public class JobLogVO implements Serializable {

    private Long id;

    private Long jobId;

    private Long traceId;

    private Date startDate;

    private String runStatus;

    private String errorMessage;
    //超时时间
    private Date timeoutDate;
    //运行参数
    private String runParam;

    private Date endDate;

    private ExecutorVO executor;
}

