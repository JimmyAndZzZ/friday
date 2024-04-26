package com.jimmy.friday.center.controller;

import com.jimmy.friday.center.core.agent.CommandSession;
import com.jimmy.friday.center.core.agent.support.CommandSupport;
import com.jimmy.friday.center.vo.agent.ExecuteCommandVO;
import com.jimmy.friday.center.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/command")
@Slf4j
public class CommandController {

    @Autowired
    private CommandSession commandSession;

    @Autowired
    private CommandSupport commandSupport;

    @GetMapping("/login")
    public Result<?> login() {
        return Result.ok(commandSession.login());
    }

    @GetMapping("/exit")
    public Result<?> exit(@RequestHeader("sessionKey") String sessionKey) {
        commandSession.exit(sessionKey);
        return Result.ok();
    }

    @PostMapping("/execute")
    public Result<?> execute(@RequestHeader("sessionKey") String sessionKey, @RequestBody ExecuteCommandVO vo) {
        return Result.ok(commandSupport.execute(vo.getCmd(), sessionKey));
    }
}
