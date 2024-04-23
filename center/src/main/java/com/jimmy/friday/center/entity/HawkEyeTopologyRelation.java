package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (HawkEyeTopologyRelation)表实体类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Data
public class HawkEyeTopologyRelation {

    @TableId(type = IdType.INPUT)
    private String id;

    private String up;

    private String down;

    private String invokeRemark;

    private String invokeType;
}

