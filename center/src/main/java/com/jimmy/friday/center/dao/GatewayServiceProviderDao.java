package com.jimmy.friday.center.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimmy.friday.center.entity.GatewayServiceProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * (GatewayServiceProvider)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-08 14:17:23
 */
@Mapper
public interface GatewayServiceProviderDao extends BaseMapper<GatewayServiceProvider> {

    boolean updateStatus(@Param("status") String status, @Param("id") Long id);
}
