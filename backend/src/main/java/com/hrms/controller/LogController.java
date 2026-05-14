package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.SysLog;
import com.hrms.mapper.SysLogMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/log")
@CrossOrigin(origins = "*")
public class LogController {

    private final SysLogMapper logMapper;

    public LogController(SysLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysLog::getOperation, keyword)
                   .or()
                   .like(SysLog::getUsername, keyword)
                   .or()
                   .like(SysLog::getMethod, keyword);
        }
        if (userId != null) {
            wrapper.eq(SysLog::getUserId, userId);
        }

        wrapper.orderByDesc(SysLog::getCreateTime);

        Page<SysLog> pageResult = new Page<>(page, pageSize);
        Page<SysLog> result = logMapper.selectPage(pageResult, wrapper);

        Map<String, Object> data = new HashMap<>();
        data.put("list", result.getRecords());
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<SysLog> getById(@PathVariable Long id) {
        return Result.success(logMapper.selectById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        logMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        // 逻辑删除全部（MyBatis-Plus的delete方法配合@TableLogic）
        List<SysLog> allLogs = logMapper.selectList(new LambdaQueryWrapper<>());
        allLogs.forEach(log -> logMapper.deleteById(log.getId()));
        return Result.success();
    }

    /**
     * 按日期范围查询
     */
    @GetMapping("/range")
    public Result<List<SysLog>> getByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate + "T00:00:00");
            java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate + "T23:59:59");
            
            LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(SysLog::getCreateTime, start)
                   .le(SysLog::getCreateTime, end)
                   .orderByDesc(SysLog::getCreateTime);

            return Result.success(logMapper.selectList(wrapper));
        } catch (Exception e) {
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = logMapper.selectCount(new LambdaQueryWrapper<>());
        stats.put("total", total);

        // 今日日志数
        java.time.LocalDate today = java.time.LocalDate.now();
        long todayCount = logMapper.selectCount(new LambdaQueryWrapper<SysLog>()
            .ge(SysLog::getCreateTime, today.atStartOfDay()));
        stats.put("todayCount", todayCount);

        // 本周日志数
        java.time.LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        long weekCount = logMapper.selectCount(new LambdaQueryWrapper<SysLog>()
            .ge(SysLog::getCreateTime, weekStart.atStartOfDay()));
        stats.put("weekCount", weekCount);

        return Result.success(stats);
    }
}
