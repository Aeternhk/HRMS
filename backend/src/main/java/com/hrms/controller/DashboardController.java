package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.Result;
import com.hrms.entity.SysUser;
import com.hrms.entity.HrDepartment;
import com.hrms.mapper.SysUserMapper;
import com.hrms.mapper.HrDepartmentMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final SysUserMapper userMapper;
    private final HrDepartmentMapper departmentMapper;

    public DashboardController(SysUserMapper userMapper, HrDepartmentMapper departmentMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 员工总数
            long totalEmployees = userMapper.selectCount(null);
            
            // 在职员工
            LambdaQueryWrapper<SysUser> activeWrapper = new LambdaQueryWrapper<>();
            activeWrapper.eq(SysUser::getStatus, 1);
            long activeEmployees = userMapper.selectCount(activeWrapper);
            
            stats.put("totalEmployees", totalEmployees);
            stats.put("activeEmployees", activeEmployees);
            stats.put("newThisMonth", 0);
            stats.put("leftThisMonth", 0);
            stats.put("probationEmployees", 0);
            stats.put("expiringContracts", 0);
            stats.put("birthdaysThisMonth", 0);
            
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    @GetMapping("/employee/trend")
    public Result<Map<String, Object>> employeeTrend(@RequestParam(defaultValue = "6") Integer months) {
        Map<String, Object> trend = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> entryData = new ArrayList<>();
        List<Integer> leaveData = new ArrayList<>();
        
        for (int i = months - 1; i >= 0; i--) {
            labels.add((i + 1) + "月前");
            entryData.add(0);
            leaveData.add(0);
        }
        
        trend.put("labels", labels);
        trend.put("entryData", entryData);
        trend.put("leaveData", leaveData);
        
        return Result.success(trend);
    }

    @GetMapping("/department/distribution")
    public Result<Map<String, Object>> departmentDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        
        List<HrDepartment> departments = departmentMapper.selectList(null);
        List<Map<String, Object>> chartData = new ArrayList<>();
        
        for (HrDepartment dept : departments) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", dept.getName());
            
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getDepartmentId, dept.getId())
                   .eq(SysUser::getStatus, 1);
            item.put("value", userMapper.selectCount(wrapper));
            chartData.add(item);
        }
        
        distribution.put("data", chartData);
        
        return Result.success(distribution);
    }

    @GetMapping("/attendance/stats")
    public Result<Map<String, Object>> attendanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("normal", 95);
        stats.put("late", 3);
        stats.put("absent", 1);
        stats.put("leave", 1);
        return Result.success(stats);
    }

    @GetMapping("/todo")
    public Result<List<Map<String, Object>>> todoList() {
        return Result.success(new ArrayList<>());
    }

    @GetMapping("/employee/recent")
    public Result<List<SysUser>> recentEmployees(@RequestParam(defaultValue = "5") Integer limit) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1)
               .orderByDesc(SysUser::getCreateTime)
               .last("LIMIT " + limit);
        List<SysUser> list = userMapper.selectList(wrapper);
        return Result.success(list);
    }

    @GetMapping("/contract/expiring")
    public Result<List<SysUser>> expiringContracts(@RequestParam(defaultValue = "30") Integer days) {
        return Result.success(new ArrayList<>());
    }
}
