package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.HrOvertime;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrOvertimeMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/overtime")
@CrossOrigin(origins = "*")
public class OvertimeController {

    private final HrOvertimeMapper overtimeMapper;
    private final SysUserMapper userMapper;

    public OvertimeController(HrOvertimeMapper overtimeMapper, SysUserMapper userMapper) {
        this.overtimeMapper = overtimeMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<HrOvertime> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(SysUser::getName, keyword)
            );
            if (users.isEmpty()) {
                Map<String, Object> data = new HashMap<>();
                data.put("list", Collections.emptyList());
                data.put("total", 0);
                data.put("page", page);
                data.put("pageSize", pageSize);
                return Result.success(data);
            }
            List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
            wrapper.in(HrOvertime::getUserId, userIds);
        }
        if (status != null) {
            wrapper.eq(HrOvertime::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(HrOvertime::getUserId, userId);
        }

        wrapper.orderByDesc(HrOvertime::getCreateTime);

        Page<HrOvertime> pageResult = new Page<>(page, pageSize);
        Page<HrOvertime> result = overtimeMapper.selectPage(pageResult, wrapper);

        // 关联用户信息
        List<Map<String, Object>> listWithUser = result.getRecords().stream().map(ot -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ot.getId());
            map.put("userId", ot.getUserId());
            map.put("overtimeDate", ot.getOvertimeDate());
            map.put("startTime", ot.getStartTime());
            map.put("endTime", ot.getEndTime());
            map.put("hours", ot.getHours());
            map.put("reason", ot.getReason());
            map.put("status", ot.getStatus());
            map.put("statusName", getStatusName(ot.getStatus()));
            map.put("approverId", ot.getApproverId());
            map.put("approveTime", ot.getApproveTime());
            map.put("createTime", ot.getCreateTime());

            SysUser user = userMapper.selectById(ot.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
                map.put("userUsername", user.getUsername());
            } else {
                map.put("userName", "未知");
                map.put("userUsername", "");
            }

            if (ot.getApproverId() != null) {
                SysUser approver = userMapper.selectById(ot.getApproverId());
                map.put("approverName", approver != null ? approver.getName() : "");
            } else {
                map.put("approverName", "");
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", listWithUser);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<HrOvertime> getById(@PathVariable Long id) {
        HrOvertime ot = overtimeMapper.selectById(id);
        return Result.success(ot);
    }

    @PostMapping
    public Result<Void> create(@RequestBody HrOvertime ot) {
        ot.setStatus(0); // 默认待审批
        ot.setCreateTime(LocalDateTime.now());
        overtimeMapper.insert(ot);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody HrOvertime ot) {
        ot.setId(id);
        overtimeMapper.updateById(ot);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        overtimeMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 审批操作
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        HrOvertime ot = overtimeMapper.selectById(id);
        if (ot == null) return Result.error("记录不存在");
        if (ot.getStatus() != 0) return Result.error("该申请已审批");

        Integer status = (Integer) body.get("status");
        String remark = (String) body.get("approveRemark");

        ot.setStatus(status);
        ot.setApproveRemark(remark);
        ot.setApproveTime(LocalDateTime.now());
        if (body.get("approverId") != null) {
            ot.setApproverId(Long.valueOf(body.get("approverId").toString()));
        } else {
            ot.setApproverId(1L);
        }
        overtimeMapper.updateById(ot);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", overtimeMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("pending", overtimeMapper.selectCount(new LambdaQueryWrapper<HrOvertime>().eq(HrOvertime::getStatus, 0)));
        stats.put("approved", overtimeMapper.selectCount(new LambdaQueryWrapper<HrOvertime>().eq(HrOvertime::getStatus, 1)));
        stats.put("rejected", overtimeMapper.selectCount(new LambdaQueryWrapper<HrOvertime>().eq(HrOvertime::getStatus, 2)));
        return Result.success(stats);
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审批";
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知";
        }
    }
}
