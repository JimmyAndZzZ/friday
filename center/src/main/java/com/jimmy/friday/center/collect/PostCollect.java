package com.jimmy.friday.center.collect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.enums.MethodTypeEnum;
import com.jimmy.friday.center.api.ApiContext;
import com.jimmy.friday.center.base.Collect;
import com.jimmy.friday.center.utils.Assert;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class PostCollect implements Collect {

    @Override
    public List<InvokeParam> collect(HttpServletRequest request, ApiContext apiContext) throws Exception {
        try (InputStream inputStream = request.getInputStream(); InputStreamReader inputStreamReader = new InputStreamReader(inputStream); BufferedReader reader = new BufferedReader(inputStreamReader)) {

            StringBuilder bodyContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                bodyContent.append(line);
            }

            List<InvokeParam> invokeParams = Lists.newArrayList();

            if (StrUtil.isNotEmpty(bodyContent)) {
                JsonNode parse = JsonUtil.parse(bodyContent.toString());

                if (parse == null) {
                    log.info("JSON解析失败:{}", bodyContent);
                }

                Assert.state(parse != null, ExceptionEnum.ERROR_PARAMETER, "参数解析失败");

                Iterator<String> names = parse.fieldNames();
                if (CollUtil.isEmpty(names)) {
                    return Lists.newArrayList();
                }

                while (names.hasNext()) {
                    String fieldName = names.next();
                    JsonNode childNode = parse.get(fieldName);

                    InvokeParam invokeParam = new InvokeParam();
                    invokeParam.setName(fieldName);
                    invokeParam.setJsonData(childNode.toString());
                    invokeParams.add(invokeParam);
                }
            }

            return invokeParams;
        }
    }

    @Override
    public MethodTypeEnum type() {
        return MethodTypeEnum.POST;
    }
}
