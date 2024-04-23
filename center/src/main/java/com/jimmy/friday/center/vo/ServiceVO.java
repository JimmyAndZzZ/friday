package com.jimmy.friday.center.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class ServiceVO implements Serializable {

    private Long id;

    private String applicationName;

    private String type;

    private String version;

    private String description;

    private BigDecimal failureRate;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastInvokeDate;

    private List<ServiceWarnVO> warns = Lists.newArrayList();

    private List<MethodVO> methods = Lists.newArrayList();

    private List<ConsumerVO> consumers = Lists.newArrayList();

    private List<ProviderVO> providers = Lists.newArrayList();

    private List<ServiceVO> otherVersions = Lists.newArrayList();

}
