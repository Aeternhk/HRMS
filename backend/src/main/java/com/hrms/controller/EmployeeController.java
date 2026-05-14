package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.HrDepartment;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrDepartmentMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final SysUserMapper userMapper;
    private final HrDepartmentMapper departmentMapper;

    public EmployeeController(SysUserMapper userMapper, HrDepartmentMapper departmentMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getName, keyword)
                   .or()
                   .like(SysUser::getUsername, keyword)  // username即工号
                   .or()
                   .like(SysUser::getPhone, keyword);
        }
        if (departmentId != null) {
            wrapper.eq(SysUser::getDepartmentId, departmentId);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        Page<SysUser> pageResult = new Page<>(page, pageSize);
        Page<SysUser> result = userMapper.selectPage(pageResult, wrapper);

        // 为每个用户添加 departmentName
        List<Map<String, Object>> listWithDeptName = result.getRecords().stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("name", user.getName());
            map.put("phone", user.getPhone());
            map.put("gender", user.getGender());
            map.put("departmentId", user.getDepartmentId());
            map.put("position", user.getPosition());
            map.put("roleId", user.getRoleId());
            map.put("status", user.getStatus());
            map.put("entryDate", user.getEntryDate());
            // 添加 departmentName
            if (user.getDepartmentId() != null) {
                HrDepartment dept = departmentMapper.selectById(user.getDepartmentId());
                map.put("departmentName", dept != null ? dept.getName() : "");
            } else {
                map.put("departmentName", "");
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", listWithDeptName);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);

        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        return Result.success(user);
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser user) {
        // 默认密码 = 工号，BCrypt加密存储
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(new BCryptPasswordEncoder().encode(user.getUsername()));
        }
        // 默认值：loginType=3(两端皆可登录)，status=1(启用)
        if (user.getLoginType() == null) {
            user.setLoginType(3);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        // 默认角色为普通员工(roleId=4)，如果不指定则自动分配
        if (user.getRoleId() == null) {
            user.setRoleId(4L);  // EMPLOYEE 角色
        }
        userMapper.insert(user);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        userMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 256);
        stats.put("newThisMonth", 18);
        stats.put("leftThisMonth", 3);
        stats.put("abnormal", 15);
        return Result.success(stats);
    }

    @GetMapping("/recent")
    public Result<List<SysUser>> recent() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysUser::getCreateTime).last("LIMIT 10");
        List<SysUser> list = userMapper.selectList(wrapper);
        return Result.success(list);
    }

    @PostMapping("/batch")
    public Result<Map<String, Object>> batchImport(@RequestBody List<SysUser> employees) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new java.util.ArrayList<>();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        for (int i = 0; i < employees.size(); i++) {
            try {
                SysUser emp = employees.get(i);
                if (emp.getUsername() == null || emp.getUsername().isEmpty()) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：工号不能为空");
                    continue;
                }
                if (emp.getName() == null || emp.getName().isEmpty()) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行：姓名不能为空");
                    continue;
                }
                // 默认密码 = 工号
                if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
                    emp.setPassword(passwordEncoder.encode(emp.getUsername()));
                }
                // 默认值
                if (emp.getLoginType() == null) emp.setLoginType(3);
                if (emp.getStatus() == null) emp.setStatus(1);
                if (emp.getRoleId() == null) emp.setRoleId(4L);  // 默认普通员工角色
                userMapper.insert(emp);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errors.add("第" + (i + 1) + "行：导入失败 - " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return Result.success(result);
    }
}
