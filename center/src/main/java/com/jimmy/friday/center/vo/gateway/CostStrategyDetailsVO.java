package com.jimmy.friday.center.vo.gateway;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * (GatewayCostStrategyDetails)表实体类
 *
 * @author makejava
 * @since 2024-01-04 13:20:57
 */
@Data
public class CostStrategyDetailsVO implements Serializable {

    private Long id;

    private BigDecimal price;

    private Integer thresholdValue;
}

