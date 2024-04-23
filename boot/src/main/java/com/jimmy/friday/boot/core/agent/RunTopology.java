package com.jimmy.friday.boot.core.agent;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class RunTopology implements Serializable {

    private Topology to;

    private Topology from;

    private String invokeRemark;

    private String invokeType;

    private String traceId;

    private Date date;
}
