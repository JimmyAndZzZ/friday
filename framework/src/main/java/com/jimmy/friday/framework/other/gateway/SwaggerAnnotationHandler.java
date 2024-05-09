package com.jimmy.friday.framework.other.gateway;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.jimmy.friday.boot.core.gateway.Param;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SwaggerAnnotationHandler {

    public void methodHandler(Method method, com.jimmy.friday.boot.core.gateway.Method m) {
        ApiOperation apiOperation = AnnotationUtils.getAnnotation(method, ApiOperation.class);
        if (apiOperation != null) {
            m.setDesc(apiOperation.value());
        }

        ApiImplicitParams apiImplicitParams = AnnotationUtils.getAnnotation(method, ApiImplicitParams.class);
        if (apiImplicitParams != null) {
            ApiImplicitParam[] value = apiImplicitParams.value();

            if (ArrayUtil.isNotEmpty(value)) {
                Map<String, ApiImplicitParam> paramMap = new HashMap<>();

                for (ApiImplicitParam apiImplicitParam : value) {
                    paramMap.put(apiImplicitParam.name(), apiImplicitParam);
                }

                List<Param> params = m.getParams();
                if (CollUtil.isNotEmpty(params)) {
                    for (Param param : params) {
                        ApiImplicitParam apiImplicitParam = paramMap.get(param.getName());
                        if (apiImplicitParam != null) {
                            param.setIsRequire(apiImplicitParam.required());
                            param.setDesc(apiImplicitParam.value());
                            param.setDefaultValue(apiImplicitParam.defaultValue());
                        }
                    }
                }

                List<Param> httpPathParams = m.getHttpPathParams();
                if (CollUtil.isNotEmpty(httpPathParams)) {
                    for (Param param : httpPathParams) {
                        ApiImplicitParam apiImplicitParam = paramMap.get(param.getName());
                        if (apiImplicitParam != null) {
                            param.setIsRequire(apiImplicitParam.required());
                            param.setDesc(apiImplicitParam.value());
                            param.setDefaultValue(apiImplicitParam.defaultValue());
                        }
                    }
                }
            }
        }
    }

    public void parameterHandler(Parameter parameter, Param param) {
        ApiParam apiParam = AnnotationUtils.getAnnotation(parameter, ApiParam.class);
        if (apiParam != null) {
            param.setDesc(apiParam.value());
            param.setIsRequire(apiParam.required());
            param.setDefaultValue(apiParam.defaultValue());
        }
    }

}
