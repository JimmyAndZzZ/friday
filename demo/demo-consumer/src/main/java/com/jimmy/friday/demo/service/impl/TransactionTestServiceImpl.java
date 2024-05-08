package com.jimmy.friday.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jimmy.friday.demo.service.TransactionTestService;
import com.jimmy.friday.boot.enums.transaction.TransactionTypeEnum;
import com.jimmy.friday.demo.component.TransactionComponent;
import com.jimmy.friday.demo.dao.TransactionTestDao;
import com.jimmy.friday.demo.entity.TransactionTest;
import com.jimmy.friday.framework.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * (TransactionTest)表服务实现类
 *
 * @author makejava
 * @since 2024-01-24 13:50:02
 */
@Slf4j
@Service("transactionTestService")
public class TransactionTestServiceImpl extends ServiceImpl<TransactionTestDao, TransactionTest> implements TransactionTestService {

    @Autowired
    private TransactionComponent transactionComponent;

    @Override
    @Transactional(type = TransactionTypeEnum.LCN)
    public void lcn() {
        TransactionTest transactionTest = new TransactionTest();
        transactionTest.setName("1");
        this.save(transactionTest);

        transactionComponent.tcc(2, 3L);
    }
}

