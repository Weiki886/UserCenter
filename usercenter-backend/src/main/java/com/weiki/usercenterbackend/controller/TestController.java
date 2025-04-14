package com.weiki.usercenterbackend.controller;

import com.weiki.usercenterbackend.common.ResultUtils;
import com.weiki.usercenterbackend.model.response.BaseResponse;
import com.weiki.usercenterbackend.service.DistributedLockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 测试接口
 */
@RestController
@RequestMapping("/test")
@Api(tags = "测试相关接口")
@Slf4j
public class TestController {

    @Resource
    private DistributedLockService distributedLockService;

    /**
     * 测试分布式锁
     * 
     * @param key 锁键
     * @param sleepMillis 模拟业务处理时间(毫秒)
     * @return 处理结果
     */
    @GetMapping("/lock")
    @ApiOperation(value = "测试分布式锁", notes = "演示如何手动使用分布式锁")
    public BaseResponse<String> testLock(
            @RequestParam(defaultValue = "test:lock") String key,
            @RequestParam(defaultValue = "2000") long sleepMillis) {
        
        String lockKey = "weiki:lock:" + key;
        log.info("尝试获取分布式锁，键: {}", lockKey);
        
        boolean locked = false;
        try {
            // 尝试获取锁，最多等待3秒，持有锁30秒，非公平锁
            locked = distributedLockService.tryLock(lockKey, 3000, 30000, TimeUnit.MILLISECONDS, false);
            
            if (!locked) {
                return ResultUtils.success("获取锁失败，请稍后再试");
            }
            
            log.info("获取锁成功，键: {}, 开始处理业务逻辑", lockKey);
            
            // 模拟业务处理
            Thread.sleep(sleepMillis);
            
            log.info("业务处理完成，键: {}", lockKey);
            return ResultUtils.success("处理成功，耗时: " + sleepMillis + "ms");
            
        } catch (InterruptedException e) {
            log.error("处理被中断: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return ResultUtils.error(5000, "处理被中断", null);
        } catch (Exception e) {
            log.error("处理异常: {}", e.getMessage());
            return ResultUtils.error(5000, "处理异常: " + e.getMessage(), null);
        } finally {
            if (locked) {
                distributedLockService.unlock(lockKey);
                log.info("释放锁，键: {}", lockKey);
            }
        }
    }
} 