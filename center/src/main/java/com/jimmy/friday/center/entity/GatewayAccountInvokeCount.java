package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (GatewayAccountInvokeCount)表实体类
 *
 * @author makejava
 * @since 2024-01-09 14:52:36
 */
@Data
public class GatewayAccountInvokeCount  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private Integer invokeDate;

    private Integer invokeCount;
}

