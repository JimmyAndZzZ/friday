package com.jimmy.friday.center.core.gateway.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Maps;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.center.base.gateway.File;
import com.jimmy.friday.center.base.Initialize;
import com.jimmy.friday.center.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FileSupport implements Initialize {

    private final Map<String, File> fileMap = Maps.newHashMap();

    public int getPage(java.io.File file) {
        String s = FileUtil.extName(file);
        File f = fileMap.get(s.toUpperCase());
        Assert.state(f != null, ExceptionEnum.ERROR_FILE, "不支持该类型文件");

        return f.getPage(file);
    }

    @Override
    public void init(ApplicationContext applicationContext) throws Exception {
        Map<String, File> beansOfType = applicationContext.getBeansOfType(File.class);
        beansOfType.values().forEach(bean -> {
            List<String> suffix = bean.suffix();
            if (CollUtil.isNotEmpty(suffix)) {
                for (String s : suffix) {
                    this.fileMap.put(s.toUpperCase(), bean);
                }
            }
        });
    }

    @Override
    public int sort() {
        return 0;
    }
}
