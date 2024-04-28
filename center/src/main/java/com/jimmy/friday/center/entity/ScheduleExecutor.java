package com.jimmy.friday.center.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (ScheduleExecutor)表实体类
 *
 * @author makejava
 * @since 2024-04-28 15:32:14
 */
@Data
public class ScheduleExecutor  {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String status;

    private String applicationName;

    private String ipAddress;

    private String description;
}

