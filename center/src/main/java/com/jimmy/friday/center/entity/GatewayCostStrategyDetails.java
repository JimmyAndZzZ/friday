package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


/**
 * (GatewayCostStrategyDetails)表实体类
 *
 * @author makejava
 * @since 2024-01-04 13:20:57
 */
@Data
public class GatewayCostStrategyDetails  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long strategyId;

    private Date createDate;

    private BigDecimal price;

    private Integer thresholdValue;
}

