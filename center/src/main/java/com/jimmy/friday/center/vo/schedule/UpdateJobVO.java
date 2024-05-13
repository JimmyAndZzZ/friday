package com.jimmy.friday.center.vo.schedule;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UpdateJobVO implements Serializable {

    private Long id;

    private String cron;

    private String description;

    private String runParam;
    //阻塞处理策略
    private String blockStrategy;
    //超时时间
    private Long timeout;
    //重试次数
    private Integer retryCount;

    private String status;

    private Integer shardingNum;
}
