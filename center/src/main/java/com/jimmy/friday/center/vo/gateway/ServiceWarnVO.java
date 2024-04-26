package com.jimmy.friday.center.vo.gateway;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


/**
 * (GatewayServiceWarn)表实体类
 *
 * @author makejava
 * @since 2024-04-16 14:05:52
 */
@Data
public class ServiceWarnVO {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    private String type;

    private String message;
}

