package com.jimmy.friday.center.controller;

import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.exception.GatewayException;
import com.jimmy.friday.center.core.gateway.api.ApiResponse;
import com.jimmy.friday.center.core.gateway.ApiComponent;
import com.jimmy.friday.center.exception.OpenApiException;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Api(tags = "对外API")
@Slf4j
public class ApiController {

    @Autowired
    private ApiComponent apiComponent;

    @RequestMapping(value = "/run/{action}", method = {RequestMethod.GET, RequestMethod.POST})
    public ApiResponse run(HttpServletRequest httpServletRequest, @PathVariable("action") String action) {
        try {
            return apiComponent.run(httpServletRequest, action);
        } catch (OpenApiException e) {
            return ApiResponse.fail(e);
        } catch (GatewayException e) {
            return ApiResponse.fail(e);
        } catch (Throwable e) {
            log.error("api调用失败", e);
            return ApiResponse.fail(ExceptionEnum.SYSTEM_ERROR, "请稍后重试");
        }
    }
}
