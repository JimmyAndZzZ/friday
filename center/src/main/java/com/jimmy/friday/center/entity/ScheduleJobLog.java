package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (ScheduleJobLog)表实体类
 *
 * @author makejava
 * @since 2024-05-11 14:12:21
 */
@Data
public class ScheduleJobLog  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long jobId;

    private Long traceId;

    private Long executorId;

    private Long startDate;

    private String runStatus;

    private String errorMessage;
//超时时间
    private Long timeoutDate;
//运行参数
    private String runParam;

    private Long endDate;

    private String jobCode;

    private Integer shardingNum;

    private Integer currentShardingNum;
}

