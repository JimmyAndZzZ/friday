package com.jimmy.friday.center.vo.gateway;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class MethodDetailVO implements Serializable {

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

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastInvokeDate;

    private MethodOpenVO methodOpenDetail;

    private List<MethodParamVO> methodParams = Lists.newArrayList();

    private List<MetricsVO> everydayMetrics = Lists.newArrayList();

    private List<MetricsVO> historyMetrics = Lists.newArrayList();
}
