package com.jimmy.friday.center.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimmy.friday.center.entity.ScheduleJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (ScheduleJobLog)表数据库访问层
 *
 * @author makejava
 * @since 2024-05-06 15:10:28
 */
@Mapper
public interface ScheduleJobLogDao extends BaseMapper<ScheduleJobLog> {

    boolean fail(@Param("runStatus") String runStatus,
                 @Param("endDate") Long endDate,
                 @Param("errorMessage") String errorMessage,
                 @Param("id") Long id);
}

