package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (GatewayPushChannelSub)表实体类
 *
 * @author makejava
 * @since 2024-02-19 11:37:51
 */
@Data
public class GatewayPushChannelSub {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private String channelName;
}

