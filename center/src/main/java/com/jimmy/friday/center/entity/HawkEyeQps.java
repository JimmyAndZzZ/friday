package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (HawkEyeQps)表实体类
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
@Data
public class HawkEyeQps  {

    @TableId(type = IdType.INPUT)
    private String id;

    private String moduleName;

    private Date createDate;

    private String requestPoint;

    private String requestAttachment;

    private String protocol;
}

