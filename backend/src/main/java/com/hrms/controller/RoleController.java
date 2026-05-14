package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.Result;
import com.hrms.entity.SysRole;
import com.hrms.mapper.SysRoleMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
@CrossOrigin(origins = "*")
public class RoleController {

    private final SysRoleMapper roleMapper;

    public RoleController(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        List<SysRole> list = roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleMapper.selectById(id);
        return Result.success(role);
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysRole role) {
        // 检查编码是否重复
        SysRole existing = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, role.getCode()));
        if (existing != null) {
            return Result.error("角色编码已存在");
        }
        roleMapper.insert(role);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysRole role) {
        role.setId(id);
        // 检查编码是否重复（排除自身）
        SysRole existing = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, role.getCode())
                .ne(SysRole::getId, id)
        );
        if (existing != null) {
            return Result.error("角色编码已存在");
        }
        roleMapper.updateById(role);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 超级管理员角色不能删除
        SysRole role = roleMapper.selectById(id);
        if (role != null && "SUPER_ADMIN".equals(role.getCode())) {
            return Result.error("不能删除超级管理员角色");
        }
        roleMapper.deleteById(id);
        return Result.success();
    }
}
