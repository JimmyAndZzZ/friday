package com.jimmy.friday.center.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimmy.friday.center.entity.ScheduleJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (ScheduleJob)表数据库访问层
 *
 * @author makejava
 * @since 2024-04-24 17:29:27
 */
@Mapper
public interface ScheduleJobDao extends BaseMapper<ScheduleJob> {

    void updateExecuteTime(@Param("lastTime") Long lastTime, @Param("nextTime") Long nextTime, @Param("id") Integer id);

    void updateNextExecuteTime(@Param("nextTime") Long nextTime, @Param("id") Integer id);

    void updateStatus(@Param("status") String status, @Param("id") Integer id);

}

