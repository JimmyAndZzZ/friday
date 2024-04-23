package com.jimmy.friday.center.exception;

import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.core.gateway.GatewayResponse;
import com.jimmy.friday.boot.exception.GatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * @author zhanglei
 * @className GlobalExceptionHandler
 * @description 系统全局异常处理
 * @date 2018-05-24 18:20
 */

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> allExceptionHandler(HttpServletRequest request, Exception exception) {
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String parameter = StrUtil.trimToNull(request.getParameter(name));
            if (parameter != null) {
                log.error("allExceptionHandler key {} value {}", name, parameter);
            }
        }
        String url = request.getRequestURI();
        log.error("system exception,url:[{}],msg:", url, exception);
        return ResponseEntity.ok(GatewayResponse.fail("系统繁忙，请稍后重试"));
    }

    @ExceptionHandler(value = GatewayException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> gatewayExceptionHandler(GatewayException exception) {
        return ResponseEntity.ok(GatewayResponse.fail(exception));
    }
}
