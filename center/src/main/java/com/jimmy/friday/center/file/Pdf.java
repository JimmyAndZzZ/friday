package com.jimmy.friday.center.file;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.center.base.File;
import com.jimmy.friday.center.exception.OpenApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class Pdf implements File {

    @Override
    public List<String> suffix() {
        return Lists.newArrayList("pdf");
    }

    @Override
    public int getPage(java.io.File file) {
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            log.error("pdf读取失败", e);
            throw new OpenApiException(ExceptionEnum.ERROR_FILE.getCode(), StrUtil.format(ExceptionEnum.ERROR_FILE.getMessage(), "pdf读取失败"));
        }
    }
}
