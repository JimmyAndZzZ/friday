package com.jimmy.friday.center.core.gateway.collect;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.MethodTypeEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;
import com.jimmy.friday.center.base.Collect;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class QueryCollect implements Collect {

    @Override
    public List<InvokeParam> collect(HttpServletRequest request, ApiContext apiContext) throws Exception {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (MapUtil.isEmpty(paramMap)) {
            return Lists.newArrayList();
        }

        List<InvokeParam> invokeParams = Lists.newArrayList();

        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            String paramName = entry.getKey();
            String[] paramValues = entry.getValue();

            if (ArrayUtil.isNotEmpty(paramValues)) {
                InvokeParam invokeParam = new InvokeParam();
                invokeParam.setName(paramName);
                invokeParam.setJsonData(JsonUtil.toString(paramValues[0]));
                invokeParams.add(invokeParam);
            }
        }

        return invokeParams;
    }

    @Override
    public MethodTypeEnum type() {
        return MethodTypeEnum.QUERY;
    }
}
