package com.jimmy.friday.demo.controller;

import com.jimmy.friday.boot.core.schedule.ScheduleInvokeResult;
import com.jimmy.friday.framework.Schedule;
import com.jimmy.friday.framework.annotation.gateway.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/demo")
@RestController
@Slf4j
public class DemoController {

    @GetMapping("delete")
    public String delete(@RequestParam(value = "str", required = false) String str) {
        Schedule.RemoveBuild remove = com.jimmy.friday.framework.Schedule.remove();
        remove.setScheduleId(str).remove();
        return "succ";
    }

    @GetMapping("append")
    public String append() {
        com.jimmy.friday.framework.Schedule.AppendBuild append = com.jimmy.friday.framework.Schedule.append();

        append.setScheduleId("run2").setCron("0/2 * * * * ?").submit(scheduleContext -> {
            log.info("run2:{}", scheduleContext.getTraceId());
            return ScheduleInvokeResult.ok();
        }).append();
        return "succ";
    }

}

