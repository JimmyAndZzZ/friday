package com.jimmy.friday.demo.component;

import cn.hutool.core.thread.ThreadUtil;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import com.jimmy.friday.framework.annotation.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionComponent {


    @Transactional(type = TransactionTypeEnum.TCC, cancelMethod = "tccCancel", confirmMethod = "tccConfirm")
    public void tcc(Integer i, long l) {
        log.info("tcc+++++i:{},l:{}", i, l);

        ThreadUtil.sleep(10000);
    }

    public void tccCancel(Integer i, long l) {
        log.info("cancel+++i:{},l:{}", i, l);
    }

    public void tccConfirm(Integer i, long l) {
        log.info("confirm+++i:{},l:{}", i, l);
    }
}
