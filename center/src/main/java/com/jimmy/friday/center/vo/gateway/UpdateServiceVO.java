package com.jimmy.friday.center.vo.gateway;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpdateServiceVO implements Serializable {

    private Long id;

    private String description;

    private String groupName;
}
