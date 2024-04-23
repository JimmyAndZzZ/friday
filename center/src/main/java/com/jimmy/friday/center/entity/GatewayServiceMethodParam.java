package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (GatewayServiceMethodParam)表实体类
 *
 * @author makejava
 * @since 2024-03-26 17:55:21
 */
@Data
public class GatewayServiceMethodParam  {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serviceId;

    private Long methodId;

    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`desc`")
    private String desc;

    private String paramType;
}

