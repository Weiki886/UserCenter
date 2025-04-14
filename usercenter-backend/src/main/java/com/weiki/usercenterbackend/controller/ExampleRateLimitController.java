package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.annotation.RateLimit;
import com.weiki.usercenterbackend.common.DefaultFallbackImpl;
import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.model.response.BaseResponse;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 限流示例接口
 * 用于演示各种限流配置
 */
@RestController
@RequestMapping("/api/demo/ratelimit")
public class ExampleRateLimitController {
    
    /**
     * 默认限流配置，使用系统默认值
     */
    @GetMapping("/default")
    @RateLimit
    public BaseResponse<Map<String, Object>> defaultRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "默认限流配置成功通过");
        data.put("config", "使用系统默认配置 (5 QPS)");
        return ResultUtils.success(data);
    }
    
    /**
     * 自定义QPS限流
     */
    @GetMapping("/customQps")
    @RateLimit(qps = 2.0)
    public BaseResponse<Map<String, Object>> customQpsRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "自定义QPS限流配置成功通过");
        data.put("config", "QPS = 2.0");
        return ResultUtils.success(data);
    }
    
    /**
     * 用户级别限流
     */
    @GetMapping("/user")
    @RateLimit(limitType = RateLimit.LimitType.USER)
    public BaseResponse<Map<String, Object>> userRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "用户级别限流配置成功通过");
        data.put("config", "每个用户单独限流");
        return ResultUtils.success(data);
    }
    
    /**
     * 全局限流
     */
    @GetMapping("/global")
    @RateLimit(limitType = RateLimit.LimitType.DEFAULT)
    public BaseResponse<Map<String, Object>> globalRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "全局限流配置成功通过");
        data.put("config", "使用全局限流器");
        return ResultUtils.success(data);
    }
    
    /**
     * 带预热期的限流
     */
    @GetMapping("/warmup")
    @RateLimit(qps = 10.0, warmupPeriod = 3, warmupUnit = TimeUnit.SECONDS)
    public BaseResponse<Map<String, Object>> warmupRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "预热限流配置成功通过");
        data.put("config", "QPS = 10.0, 预热期 = 3秒");
        return ResultUtils.success(data);
    }
    
    /**
     * 阻塞式限流
     */
    @GetMapping("/blocking")
    @RateLimit(qps = 5.0, timeout = -1)
    public BaseResponse<Map<String, Object>> blockingRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "阻塞式限流配置成功通过");
        data.put("config", "所有请求都会等待获取令牌");
        return ResultUtils.success(data);
    }
    
    /**
     * 超时等待限流
     */
    @GetMapping("/timeout")
    @RateLimit(qps = 5.0, timeout = 500, timeoutUnit = TimeUnit.MILLISECONDS)
    public BaseResponse<Map<String, Object>> timeoutRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "超时等待限流配置成功通过");
        data.put("config", "最多等待500毫秒");
        return ResultUtils.success(data);
    }
    
    /**
     * 动态QPS限流
     */
    @GetMapping("/dynamic/{qps}")
    @RateLimit(qps = 5.0)
    public BaseResponse<Map<String, Object>> dynamicRateLimit(@PathVariable("qps") int qps) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "动态QPS限流配置成功通过");
        data.put("requestedQps", qps);
        data.put("actualQps", 5.0);
        return ResultUtils.success(data);
    }
    
    /**
     * 带降级处理的限流
     */
    @GetMapping("/fallback")
    @RateLimit(qps = 1.0, fallbackClass = DefaultFallbackImpl.class)
    public BaseResponse<Map<String, Object>> rateLimitWithFallback() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "带降级处理的限流配置成功通过");
        data.put("config", "触发限流时会返回降级结果");
        return ResultUtils.success(data);
    }
    
    /**
     * 支付接口 - 严格限流100 QPS (预热10秒)
     * 生产环境关键接口示例
     */
    @RequestMapping(value = "/api/v1/payment", method = {RequestMethod.GET, RequestMethod.POST})
    @RateLimit(qps = 100.0, warmupPeriod = 10, warmupUnit = TimeUnit.SECONDS, 
               timeout = 200, timeoutUnit = TimeUnit.MILLISECONDS)
    public BaseResponse<Map<String, Object>> paymentEndpoint() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "支付接口请求成功");
        data.put("config", "严格限流：100 QPS, 预热期 10秒");
        return ResultUtils.success(data);
    }
    
    /**
     * 商品接口 - 弹性限流 (500 QPS 基础 + 突发流量200%容忍)
     * 高流量接口示例
     */
    @RequestMapping(value = "/api/v1/product", method = {RequestMethod.GET, RequestMethod.POST})
    @RateLimit(qps = 500.0, priority = 8)
    public BaseResponse<Map<String, Object>> productEndpoint() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "商品接口请求成功");
        data.put("config", "弹性限流：500 QPS 基础，突发容忍200%");
        return ResultUtils.success(data);
    }
    
    /**
     * 用户付费级别限流（优先级演示）
     * 付费用户优先级是免费用户的3倍（暂未启用）
     */
    @GetMapping("/user/premium")
    @RateLimit(limitType = RateLimit.LimitType.USER, priority = 10)
    public BaseResponse<Map<String, Object>> premiumUserRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "付费用户请求成功");
        data.put("config", "优先级为10（免费用户为3）");
        data.put("status", "付费用户拥有更高的请求优先级");
        return ResultUtils.success(data);
    }
    
    @GetMapping("/user/free")
    @RateLimit(limitType = RateLimit.LimitType.USER, priority = 3)
    public BaseResponse<Map<String, Object>> freeUserRateLimit() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "免费用户请求成功");
        data.put("config", "优先级为3（付费用户为10）");
        data.put("status", "高峰期可能会被限流");
        return ResultUtils.success(data);
    }
} 