package com.jimmy.friday.center.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;


/**
 * (TransactionPoint)表实体类
 *
 * @author makejava
 * @since 2024-01-19 14:54:40
 */
@Data
public class TransactionPoint {

    @TableId(type = IdType.INPUT)
    private String id;

    private Date createDate;

    private String status;

    private Integer timeout;

    private Long timeoutTimestamp;
}

