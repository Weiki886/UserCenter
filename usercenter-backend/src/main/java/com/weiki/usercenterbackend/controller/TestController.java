package com.weiki.usercenterbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
@Api(tags = "测试接口")
public class TestController {

    @GetMapping("/hello")
    @ApiOperation(value = "Hello接口", notes = "返回Hello World字符串")
    public String hello() {
        return "Hello World!";
    }
} 