package com.jimmy.friday.center.base;

import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.MethodTypeEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface Collect {

    List<InvokeParam> collect(HttpServletRequest request, ApiContext apiContext) throws Exception;

    MethodTypeEnum type();
}
