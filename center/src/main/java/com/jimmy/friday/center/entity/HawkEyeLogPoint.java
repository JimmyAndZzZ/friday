package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (HawkEyeLogPoint)表实体类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Data
public class HawkEyeLogPoint  {

    @TableId(type = IdType.INPUT)
    private String id;

    private String traceId;

    private String className;

    private String methodName;

    private String applicationName;

    private Date createDate;
}

