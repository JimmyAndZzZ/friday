package com.jimmy.friday.center.core.gateway.collect;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.core.gateway.InvokeParam;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.boot.enums.MethodTypeEnum;
import com.jimmy.friday.boot.other.AttributeConstants;
import com.jimmy.friday.center.core.gateway.api.ApiContext;
import com.jimmy.friday.center.base.gateway.Collect;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.center.utils.Assert;
import com.jimmy.friday.center.utils.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FileCollect implements Collect {

    @Override
    public List<InvokeParam> collect(HttpServletRequest request, ApiContext apiContext) throws Exception {
        Long traceId = apiContext.getLong(ApiConstants.CONTEXT_PARAM_TRACE_ID);
        //加载文件
        FileInfo fileInfo = this.getFile(request, traceId);

        File file = fileInfo.getFile();
        String fileName = fileInfo.getFileName();
        //注册钩子
        apiContext.put(ApiConstants.CONTEXT_PARAM_FILE_PATH, file.getPath());
        apiContext.register(() -> FileUtil.del(file));

        List<InvokeParam> invokeParams = Lists.newArrayList();

        InvokeParam filePathParam = new InvokeParam();
        filePathParam.setName(ApiConstants.CONTEXT_PARAM_FILE_PATH);
        filePathParam.setJsonData(file.getPath());
        invokeParams.add(filePathParam);

        InvokeParam fileNameParam = new InvokeParam();
        fileNameParam.setName(ApiConstants.CONTEXT_PARAM_FILE_NAME);
        fileNameParam.setJsonData(fileName);
        invokeParams.add(fileNameParam);

        Map<String, String[]> paramMap = request.getParameterMap();
        if (MapUtil.isNotEmpty(paramMap)) {
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                String paramName = entry.getKey();
                String[] paramValues = entry.getValue();

                if (AttributeConstants.Http.HTTP_MULTIPART_FILE_PARAM_NAME.equals(paramName)) {
                    continue;
                }

                if (ArrayUtil.isNotEmpty(paramValues)) {
                    InvokeParam invokeParam = new InvokeParam();
                    invokeParam.setName(paramName);
                    invokeParam.setJsonData(JsonUtil.toString(paramValues[0]));
                    invokeParams.add(invokeParam);
                }
            }
        }

        return invokeParams;
    }

    /**
     * 获取文件
     *
     * @param request
     * @return
     * @throws IOException
     */
    private FileInfo getFile(HttpServletRequest request, Long traceId) throws IOException {
        Assert.state(request instanceof MultipartHttpServletRequest, ExceptionEnum.ERROR_FILE, "请上传文件");

        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFile = multipartHttpServletRequest.getFile(AttributeConstants.Http.HTTP_MULTIPART_FILE_PARAM_NAME);

        Assert.state(multipartFile != null, ExceptionEnum.ERROR_FILE, "请上传文件");
        // 创建一个临时文件
        File touch = FileUtil.touch(traceId + "#" + multipartFile.getOriginalFilename());
        multipartFile.transferTo(touch); // 将MultipartFile的内容传输到临时文件中

        FileInfo fileInfo = new FileInfo();
        fileInfo.setFile(touch);
        fileInfo.setFileName(multipartFile.getOriginalFilename());
        return fileInfo;
    }

    @Override
    public MethodTypeEnum type() {
        return MethodTypeEnum.FILE;
    }


    @Data
    private static class FileInfo {

        private String fileName;

        private File file;

    }

}
