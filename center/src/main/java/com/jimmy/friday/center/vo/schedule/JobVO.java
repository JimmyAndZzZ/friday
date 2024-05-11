package com.jimmy.friday.center.vo.schedule;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * (ScheduleJob)表实体类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Data
public class JobVO implements Serializable {

    private Long id;
    //任务执行CRON
    private String cron;

    private String description;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateDate;
    //唯一标识
    private String code;
    //运行参数
    private String runParam;
    //阻塞处理策略
    private String blockStrategy;
    //超时时间
    private Long timeout;
    //重试次数
    private Integer retryCount;

    private String status;
    //上次执行时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastTime;
    //下次执行时间
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextTime;

    private String applicationName;

    private String isManual;

    private String source;

}

