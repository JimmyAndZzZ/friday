package com.jimmy.friday.demo.controller;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.jimmy.friday.demo.dto.TestDTO;
import com.jimmy.friday.demo.fallback.DemoFallback;
import com.jimmy.friday.demo.vo.NewsContentVO;
import com.jimmy.friday.framework.annotation.gateway.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/demo")
@RestController
@Slf4j
public class DemoController {

    @GetMapping("abc")
    @Api(id = "abc")
    public String abc(@RequestParam(value = "str", required = false) String str) {
        return "succ";
    }

    @GetMapping("/bc/{ss}")
    @Api(id = "bc", fallbackMethod = "bcFallback", fallbackClass = DemoFallback.class)
    public String bc(@RequestParam("id") Long id, @RequestParam(value = "str", required = false) String str, @PathVariable(name = "ss") String ss) {
        if (StrUtil.isNotEmpty("123")) {
            throw new RuntimeException("123");
        }

        return "success";
    }

    @PostMapping("ab/{ss}/{id}")
    @Api(id = "ab")
    public Long ab(@RequestBody TestDTO testDTO, @PathVariable(name = "ss") String ss, @PathVariable Long id) {
        log.info("收到ab:{}", testDTO);
        log.info("收到ss:{}", ss);
        log.info("收到id:{}", id);
        ThreadUtil.sleep(10000);
        throw new RuntimeException("123");
    }

    @PostMapping("uploadDemo")
    @Api(id = "uploadDemo")
    public Long uploadDemo(@RequestParam("file") MultipartFile multipartFile) {
        log.info("multipartFile:" + multipartFile.getOriginalFilename());
        return 1L;
    }

    @PostMapping("/addDemo")
    @Api(id = "addDemo")
    public String addDemo(@RequestBody NewsContentVO newsContentDTO) {
        log.info("123:{}", newsContentDTO);
        return "123";
    }
}

