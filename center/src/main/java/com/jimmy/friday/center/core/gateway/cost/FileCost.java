package com.jimmy.friday.center.core.gateway.cost;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.boot.enums.ChargeTypeEnum;
import com.jimmy.friday.boot.enums.ExceptionEnum;
import com.jimmy.friday.center.core.gateway.api.ApiContext;
import com.jimmy.friday.center.entity.GatewayCostStrategyDetails;
import com.jimmy.friday.center.core.gateway.support.FileSupport;
import com.jimmy.friday.boot.other.ApiConstants;
import com.jimmy.friday.center.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class FileCost extends BaseCost {

    @Autowired
    private FileSupport fileSupport;

    @Override
    public BigDecimal calculate(Long costStrategyId, String appId, String action, ApiContext apiContext) {
        List<GatewayCostStrategyDetails> gatewayCostStrategyDetails = gatewayCostStrategyDetailsService.queryByCostStrategyId(costStrategyId);
        if (CollUtil.isEmpty(gatewayCostStrategyDetails)) {
            return new BigDecimal(0);
        }

        String filePath = apiContext.get(ApiConstants.CONTEXT_PARAM_FILE_PATH, String.class);
        Assert.state(StrUtil.isNotEmpty(filePath), ExceptionEnum.ERROR_FILE, "目标文件不存在");

        int page = fileSupport.getPage(FileUtil.newFile(filePath));
        Assert.state(page > 0, ExceptionEnum.ERROR_FILE, "文件读取页数失败");

        apiContext.put(ApiConstants.CONTEXT_INCREMENT_COUNT, page);
        return super.calculate(appId, action, gatewayCostStrategyDetails, page);
    }

    @Override
    public ChargeTypeEnum type() {
        return ChargeTypeEnum.PAGE;
    }
}
