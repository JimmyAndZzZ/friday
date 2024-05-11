package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (ScheduleJob)表实体类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Data
public class ScheduleJob {

    @TableId(type = IdType.AUTO)
    private Long id;
    //任务执行CRON
    private String cron;

    private String description;

    private Date createDate;

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
    private Long lastTime;
    //下次执行时间
    private Long nextTime;

    private String applicationName;

    private String isManual;

    private String source;

    public ScheduleJob clear() {
        this.description = null;
        return this;
    }

}

