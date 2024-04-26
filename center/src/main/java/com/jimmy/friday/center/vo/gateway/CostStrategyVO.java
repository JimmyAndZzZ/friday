package com.jimmy.friday.center.vo.gateway;


import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * (GatewayCostStrategy)表实体类
 *
 * @author makejava
 * @since 2024-01-04 13:21:40
 */
@Data
public class CostStrategyVO implements Serializable {

    private Long id;

    private String name;

    private String type;

    private String chargeType;

    private List<CostStrategyDetailsVO> details = Lists.newArrayList();
}

