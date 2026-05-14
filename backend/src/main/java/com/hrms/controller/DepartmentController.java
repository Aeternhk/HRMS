package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.Result;
import com.hrms.entity.HrDepartment;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrDepartmentMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = "*")
public class DepartmentController {

    private final HrDepartmentMapper departmentMapper;
    private final SysUserMapper userMapper;

    public DepartmentController(HrDepartmentMapper departmentMapper, SysUserMapper userMapper) {
        this.departmentMapper = departmentMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/tree")
    public Result<List<HrDepartment>> tree() {
        List<HrDepartment> all = departmentMapper.selectList(new LambdaQueryWrapper<>());
        // 构建树形结构
        List<HrDepartment> tree = buildTree(all, 0L);
        return Result.success(tree);
    }

    @GetMapping("/list")
    public Result<List<HrDepartment>> list() {
        List<HrDepartment> list = departmentMapper.selectList(null);
        return Result.success(list);
    }

    @GetMapping("/employee-counts")
    public Result<Map<Long, Long>> getEmployeeCounts() {
        // 返回各部门的员工数量（包含子部门人数汇总）
        List<HrDepartment> allDepts = departmentMapper.selectList(null);
        Map<Long, Long> counts = new HashMap<>();
        
        // 统计每个部门的直属员工数量
        for (HrDepartment dept : allDepts) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getDepartmentId, dept.getId())
                   .eq(SysUser::getStatus, 1);
            long count = userMapper.selectCount(wrapper);
            counts.put(dept.getId(), count);
        }
        
        // 计算每个部门的总人数（包括子部门的员工）
        Map<Long, Long> totalCounts = new HashMap<>();
        for (HrDepartment dept : allDepts) {
            long total = getTotalEmployeeCount(dept.getId(), allDepts, counts);
            totalCounts.put(dept.getId(), total);
        }
        
        return Result.success(totalCounts);
    }

    @GetMapping("/{id}")
    public Result<HrDepartment> getById(@PathVariable Long id) {
        HrDepartment department = departmentMapper.selectById(id);
        return Result.success(department);
    }

    @PostMapping
    public Result<Void> create(@RequestBody HrDepartment department) {
        departmentMapper.insert(department);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody HrDepartment department) {
        department.setId(id);
        departmentMapper.updateById(department);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentMapper.deleteById(id);
        return Result.success();
    }

    private List<HrDepartment> buildTree(List<HrDepartment> all, Long parentId) {
        return all.stream()
                .filter(d -> {
                    Long dParent = d.getParentId();
                    if (parentId == null && dParent == null) return true;
                    if (parentId == null) return false;
                    return parentId.equals(dParent);
                })
                .peek(d -> {
                    // 递归设置 children（MyBatis-Plus 使用 @TableField(exist=false) 不影响数据库）
                    List<HrDepartment> children = buildTree(all, d.getId());
                    if (!children.isEmpty()) {
                        d.setChildren(children);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 递归计算部门总人数（包含子部门）
     */
    private long getTotalEmployeeCount(Long deptId, List<HrDepartment> allDepts, Map<Long, Long> directCounts) {
        long count = directCounts.getOrDefault(deptId, 0L);
        // 查找子部门
        for (HrDepartment dept : allDepts) {
            if (Objects.equals(dept.getParentId(), deptId)) {
                count += getTotalEmployeeCount(dept.getId(), allDepts, directCounts);
            }
        }
        return count;
    }
}
