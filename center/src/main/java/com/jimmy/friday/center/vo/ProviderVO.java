package com.jimmy.friday.center.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ProviderVO implements Serializable {

    private Long id;

    private Long serviceId;

    private Integer port;

    private Integer weight;

    private String status;

    private String ipAddress;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
}
