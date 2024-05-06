package com.jimmy.friday.center.core.gateway.support;

import com.google.common.collect.Maps;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.enums.MethodTypeEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;
import com.jimmy.friday.center.base.gateway.Collect;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Component
public class CollectSupport implements Initialize {

    private final Map<MethodTypeEnum, Collect> collectMap = Maps.newHashMap();

    public List<InvokeParam> collect(String methodType, HttpServletRequest request, ApiContext apiContext) throws Exception {
        MethodTypeEnum methodTypeEnum = MethodTypeEnum.queryByType(methodType);

        Assert.state(methodTypeEnum != null, ExceptionEnum.SYSTEM_ERROR, "服务类型异常");
        return collectMap.get(methodTypeEnum).collect(request, apiContext);
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        Map<String, Collect> beansOfType = applicationContext.getBeansOfType(Collect.class);
        beansOfType.values().forEach(bean -> collectMap.put(bean.type(), bean));
    }

    @Override
    public int sort() {
        return 0;
    }
}


