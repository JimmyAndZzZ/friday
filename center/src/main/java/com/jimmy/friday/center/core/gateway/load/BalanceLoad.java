package com.jimmy.friday.center.core.gateway.load;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.LoadTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.base.Load;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class BalanceLoad implements Load {

    @Override
    public Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum) {
        // 按照引用数排序，选择引用数最小的对象
        return serviceList.stream()
                .min(Comparator.comparingInt(bean -> bean.getReferenceCount().get()))
                .orElse(null);
    }

    @Override
    public LoadTypeEnum type() {
        return LoadTypeEnum.BALANCE;
    }
}
