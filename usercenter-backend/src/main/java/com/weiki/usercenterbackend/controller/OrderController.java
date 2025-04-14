package com.weiki.usercenterbackend.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final MeterRegistry meterRegistry;
    private Counter orderApiCounter;
    
    @PostConstruct
    public void init() {
        // 初始化自定义指标 - 订单API调用计数器
        orderApiCounter = Counter.builder("api.orders.calls")
                .description("Number of calls to orders API")
                .tag("application", "usercenter")
                .tag("endpoint", "/api/orders")
                .register(meterRegistry);
    }

    /**
     * 获取订单列表（示例接口）
     * 
     * @return 订单列表
     */
    @GetMapping
    public Map<String, Object> getOrders() {
        // 增加计数器
        orderApiCounter.increment();
        
        log.info("Orders API called");
        
        // 返回示例数据
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Order list retrieved successfully");
        response.put("data", new HashMap<>() {{
            put("orders", new String[]{"order1", "order2", "order3"});
            put("total", 3);
        }});
        
        return response;
    }
} 