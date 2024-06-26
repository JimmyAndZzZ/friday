package com.jimmy.friday.center.core.gateway.load;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.gateway.LoadTypeEnum;
import com.jimmy.friday.boot.enums.gateway.ServiceTypeEnum;
import com.jimmy.friday.center.base.gateway.Load;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class RandomLoad implements Load {

    @Override
    public Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum) {
        if (serviceList.size() == 1) {
            return serviceList.stream().findFirst().get();
        }

        Random random = new Random();
        int randomIndex = random.nextInt(serviceList.size());
        return serviceList.get(randomIndex);
    }

    @Override
    public LoadTypeEnum type() {
        return LoadTypeEnum.RANDOM;
    }
}
