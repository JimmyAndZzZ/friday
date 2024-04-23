package com.jimmy.friday.center.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimmy.friday.center.entity.TransactionPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (TransactionPoint)表数据库访问层
 *
 * @author makejava
 * @since 2024-01-19 14:54:40
 */
@Mapper
public interface TransactionPointDao extends BaseMapper<TransactionPoint> {

    boolean updateStatus(@Param("update") String update, @Param("id") String id, @Param("expect") String expect);

    boolean updateTimeout(@Param("timeout") Integer timeout, @Param("id") String id, @Param("timeoutTimestamp") Long timeoutTimestamp);

}

