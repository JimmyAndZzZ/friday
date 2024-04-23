package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (GatewayCostStrategy)表实体类
 *
 * @author makejava
 * @since 2024-01-04 13:21:40
 */
@Data
public class GatewayCostStrategy  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Date createDate;

    private String type;

    private String chargeType;
}

