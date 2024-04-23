package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (HawkEyeLog)表实体类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Data
public class HawkEyeLog {

    @TableId(type = IdType.INPUT)
    private String id;

    private String logMessage;

    private String className;

    private String methodName;

    private String traceId;

    private String spanId;

    private String level;

    private String param;

    private String result;

    private String moduleName;

    private Date createDate;

    private Date modifyDate;

    private String isLog;

    private Long appId;
}

