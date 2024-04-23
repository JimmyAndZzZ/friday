package com.jimmy.friday.demo.controller;

import com.jimmy.friday.demo.service.TransactionTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    @Autowired
    private TransactionTestService transactionTestService;

    @GetMapping("/lcn")
    public void lcn() {
        transactionTestService.lcn();
    }
}
