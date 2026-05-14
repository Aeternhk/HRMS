package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.Result;
import com.hrms.entity.SysUser;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final SysUserMapper userMapper;

    public UserController(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        List<SysUser> list = userMapper.selectList(new LambdaQueryWrapper<SysUser>().orderByDesc(SysUser::getCreateTime));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null); // 不返回密码
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysUser user) {
        // 检查用户名是否重复
        SysUser existing = userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (existing != null) {
            return Result.error("用户名已存在");
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
        // 不能删除超级管理员
        SysUser user = userMapper.selectById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            return Result.error("不能删除超级管理员");
        }
        userMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        SysUser user = userMapper.selectById(id);
        if (user != null && "admin".equals(user.getUsername())) {
            return Result.error("不能禁用超级管理员");
        }
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        user.setPassword("123456"); // 重置为默认密码
        userMapper.updateById(user);
        return Result.success();
    }
}
