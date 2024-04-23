package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * (GatewayAccountOrder)表实体类
 *
 * @author makejava
 * @since 2024-01-09 14:22:05
 */
@Data
public class GatewayAccountOrder  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private Long orderTraceNo;

    private Date createDate;

    private String status;

    private BigDecimal amount;

    private String source;

    private String trdOrderTraceNo;

    private String purpose;
}

