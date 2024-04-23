package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (GatewayServiceMethodInvokeMetrics)表实体类
 *
 * @author makejava
 * @since 2024-03-26 16:49:31
 */
@Data
public class GatewayServiceMethodInvokeMetrics  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private Long methodId;

    private String meterDate;

    private Integer invokeCount;

    private String meterUnit;

    private Date createDate;
}

