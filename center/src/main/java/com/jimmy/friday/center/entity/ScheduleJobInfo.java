package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (ScheduleJobInfo)表实体类
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Data
public class ScheduleJobInfo {

    @TableId(type = IdType.AUTO)
    private Integer id;
    //任务执行CRON
    private String cron;

    private String description;

    private Date createDate;

    private Date updateDate;
    //路由策略
    private String routeStrategy;
    //唯一标识
    private String code;
    //运行参数
    private String runParam;
    //阻塞处理策略
    private String blockStrategy;
    //超时时间
    private Integer timeout;
    //重试次数
    private Integer retryCount;

    private String status;
    //上次执行时间
    private Long lastTime;
    //下次执行时间
    private Long nextTime;
}

