package com.jimmy.friday.boot.core.agent;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Qps implements Serializable {

    private String requestPoint;

    private Date createDate;

    private String requestAttachment;

    private String protocol;
}
