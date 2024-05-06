package com.jimmy.friday.center.core.gateway;

import cn.hutool.core.map.MapUtil;
import cn.hutool.cron.CronUtil;
import com.jimmy.friday.center.base.Check;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.netty.CenterSever;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GatewayBootstrap implements InitializingBean, ApplicationRunner {

    @Autowired
    private CenterSever centerSever;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Check> checkMap = applicationContext.getBeansOfType(Check.class);
        if (MapUtil.isNotEmpty(checkMap)) {
            for (Check value : checkMap.values()) {
                Exception check = value.check();
                if (check != null) {
                    throw check;
                }
            }
        }

        Map<String, Initialize> beansOfType = applicationContext.getBeansOfType(Initialize.class);
        Collection<Initialize> values = beansOfType.values();

        List<Initialize> collect = values.stream().sorted(Comparator.comparingInt(Initialize::sort)).collect(Collectors.toList());
        for (Initialize initialize : collect) {
            initialize.init(applicationContext);
        }

        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        centerSever.start();
    }
}
