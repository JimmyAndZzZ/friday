package com.jimmy.friday.center.vo.gateway;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MethodVO implements Serializable {

    private Long id;

    private Long serviceId;

    private String name;

    private String interfaceName;

    private String returnType;

    private String paramType;

    private Integer retry;

    private Integer timeout;

    private String methodId;

    private Integer todayInvokeCount = 0;

    private Integer historyInvokeCount = 0;

    private Boolean isOpen = false;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastInvokeDate;

}
