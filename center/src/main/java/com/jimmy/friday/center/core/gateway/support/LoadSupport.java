package com.jimmy.friday.center.core.gateway.support;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.LoadTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.base.Filter;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.base.Load;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LoadSupport implements Initialize {

    private final Map<LoadTypeEnum, Load> loadMap = Maps.newHashMap();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void init() throws Exception {
        Map<String, Load> beansOfType = applicationContext.getBeansOfType(Load.class);
        beansOfType.values().forEach(bean -> loadMap.put(bean.type(), bean));
    }

    public Service load(List<Service> serviceList, LoadTypeEnum loadType, ServiceTypeEnum serviceTypeEnum, Filter<Service> serviceFilter) {
        //过滤器过滤
        if (serviceFilter != null) {
            serviceList = serviceList.stream().filter(serviceFilter::filter).collect(Collectors.toList());
            if (CollUtil.isEmpty(serviceList)) {
                return null;
            }
        }

        return loadMap.get(loadType).load(serviceList, serviceTypeEnum);
    }

    @Override
    public int sort() {
        return 0;
    }
}
