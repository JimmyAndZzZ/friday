package com.jimmy.friday.center.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jimmy.friday.center.entity.GatewayAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * (GatewayAccount)表数据库访问层
 *
 * @author makejava
 * @since 2023-12-08 14:17:14
 */
@Mapper
public interface GatewayAccountDao extends BaseMapper<GatewayAccount> {

    boolean deductBalance(@Param("cost") BigDecimal cost, @Param("uid") String uid);

    boolean rechargeBalance(@Param("cost") BigDecimal cost, @Param("uid") String uid);
}

