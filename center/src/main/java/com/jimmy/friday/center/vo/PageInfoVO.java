package com.jimmy.friday.center.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class PageInfoVO<T> implements Serializable {

    private long total = 0L;
    private long size = 10L;
    private long current = 1L;
    private List<T> records = Lists.newArrayList();

    public static <S> PageInfoVO<S> build(IPage page, Class<S> clazz) {
        PageInfoVO<S> pageInfoVO = new PageInfoVO<>();
        pageInfoVO.setCurrent(page.getCurrent());
        pageInfoVO.setSize(page.getSize());
        pageInfoVO.setTotal(page.getTotal());

        List records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            for (Object record : records) {
                pageInfoVO.getRecords().add(BeanUtil.toBean(record, clazz));
            }
        }

        return pageInfoVO;
    }
}
