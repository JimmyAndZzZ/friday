package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * (GatewayService)表实体类
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Data
public class GatewayService {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String applicationName;

    private String type;

    private String version;

    private String description;

    private String groupName;

    private Date createDate;

}

