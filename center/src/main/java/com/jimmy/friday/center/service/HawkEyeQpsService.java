package com.jimmy.friday.center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jimmy.friday.boot.message.agent.AgentQps;
import com.jimmy.friday.center.entity.HawkEyeQps;

/**
 * (HawkEyeQps)表服务接口
 *
 * @author makejava
 * @since 2023-12-22 10:30:25
 */
public interface HawkEyeQpsService extends IService<HawkEyeQps> {

    void save(AgentQps agentQps);
}

