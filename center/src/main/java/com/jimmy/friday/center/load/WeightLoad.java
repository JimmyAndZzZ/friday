package com.jimmy.friday.center.load;

import com.jimmy.friday.boot.core.gateway.Service;
import com.jimmy.friday.boot.enums.LoadTypeEnum;
import com.jimmy.friday.boot.enums.ServiceTypeEnum;
import com.jimmy.friday.center.base.Load;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class WeightLoad implements Load {

    @Override
    public Service load(List<Service> serviceList, ServiceTypeEnum serviceTypeEnum) {
        if (serviceList.size() == 1) {
            return serviceList.stream().findFirst().get();
        }

        Random random = new Random();
        int totalWeight = 0;
        // 计算总权重
        for (Service obj : serviceList) {
            totalWeight += obj.getWeight();
        }
        // 生成随机数，在区间[0, totalWeight)内进行选择
        int randomWeight = random.nextInt(totalWeight);
        // 根据权重进行选择
        int accumulatedWeight = 0;
        for (Service obj : serviceList) {
            accumulatedWeight += obj.getWeight();
            if (randomWeight < accumulatedWeight) {
                return obj;
            }
        }

        // 默认返回最后一个对象（权重之和不为0时）
        return serviceList.get(serviceList.size() - 1);
    }

    @Override
    public LoadTypeEnum type() {
        return LoadTypeEnum.WEIGHT;
    }

}
