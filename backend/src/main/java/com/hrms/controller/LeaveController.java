package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.HrLeave;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrLeaveMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = "*")
public class LeaveController {

    private final HrLeaveMapper leaveMapper;
    private final SysUserMapper userMapper;

    public LeaveController(HrLeaveMapper leaveMapper, SysUserMapper userMapper) {
        this.leaveMapper = leaveMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<HrLeave> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            // 关键词搜索员工姓名
            List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(SysUser::getName, keyword)
            );
            if (users.isEmpty()) {
                // 无匹配时返回空结果
                Map<String, Object> data = new HashMap<>();
                data.put("list", Collections.emptyList());
                data.put("total", 0);
                data.put("page", page);
                data.put("pageSize", pageSize);
                return Result.success(data);
            }
            List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
            wrapper.in(HrLeave::getUserId, userIds);
        }
        if (leaveType != null && !leaveType.isEmpty()) {
            wrapper.eq(HrLeave::getLeaveType, leaveType);
        }
        if (status != null) {
            wrapper.eq(HrLeave::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(HrLeave::getUserId, userId);
        }

        wrapper.orderByDesc(HrLeave::getCreateTime);

        Page<HrLeave> pageResult = new Page<>(page, pageSize);
        Page<HrLeave> result = leaveMapper.selectPage(pageResult, wrapper);

        // 关联用户信息
        List<Map<String, Object>> listWithUser = result.getRecords().stream().map(leave -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", leave.getId());
            map.put("userId", leave.getUserId());
            map.put("leaveType", leave.getLeaveType());
            map.put("leaveTypeName", getLeaveTypeName(leave.getLeaveType()));
            map.put("startDate", leave.getStartDate());
            map.put("endDate", leave.getEndDate());
            map.put("days", leave.getDays());
            map.put("reason", leave.getReason());
            map.put("status", leave.getStatus());
            map.put("statusName", getStatusName(leave.getStatus()));
            map.put("approverId", leave.getApproverId());
            map.put("approveTime", leave.getApproveTime());
            map.put("approveRemark", leave.getApproveRemark());
            map.put("createTime", leave.getCreateTime());

            // 查询员工信息
            SysUser user = userMapper.selectById(leave.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
                map.put("userUsername", user.getUsername());
                map.put("departmentName", "");
            } else {
                map.put("userName", "未知");
                map.put("userUsername", "");
                map.put("departmentName", "");
            }

            // 审批人信息
            if (leave.getApproverId() != null) {
                SysUser approver = userMapper.selectById(leave.getApproverId());
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
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        HrLeave leave = leaveMapper.selectById(id);
        if (leave == null) return Result.error("记录不存在");

        Map<String, Object> map = new HashMap<>();
        map.put("id", leave.getId());
        map.put("userId", leave.getUserId());
        map.put("leaveType", leave.getLeaveType());
        map.put("startDate", leave.getStartDate());
        map.put("endDate", leave.getEndDate());
        map.put("days", leave.getDays());
        map.put("reason", leave.getReason());
        map.put("status", leave.getStatus());
        map.put("approverId", leave.getApproverId());
        map.put("approveTime", leave.getApproveTime());
        map.put("approveRemark", leave.getApproveRemark());
        map.put("createTime", leave.getCreateTime());

        SysUser user = userMapper.selectById(leave.getUserId());
        if (user != null) {
            map.put("userName", user.getName());
        }

        return Result.success(map);
    }

    @PostMapping
    public Result<Void> create(@RequestBody HrLeave leave) {
        leave.setStatus(0); // 默认待审批
        leave.setCreateTime(LocalDateTime.now());
        leaveMapper.insert(leave);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody HrLeave leave) {
        leave.setId(id);
        leaveMapper.updateById(leave);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        leaveMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 审批操作
     * @param id 请假ID
     * @param body 包含 status(1=通过 2=驳回) 和 approveRemark
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        HrLeave leave = leaveMapper.selectById(id);
        if (leave == null) return Result.error("记录不存在");
        if (leave.getStatus() != 0) return Result.error("该申请已审批");

        Integer status = (Integer) body.get("status");
        String remark = (String) body.get("approveRemark");
        
        leave.setStatus(status);
        leave.setApproveRemark(remark);
        leave.setApproveTime(LocalDateTime.now());
        // 审批人ID从请求中获取（简化处理，默认为admin）
        if (body.get("approverId") != null) {
            leave.setApproverId(Long.valueOf(body.get("approverId").toString()));
        } else {
            leave.setApproverId(1L);
        }
        leaveMapper.updateById(leave);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", leaveMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("pending", leaveMapper.selectCount(new LambdaQueryWrapper<HrLeave>().eq(HrLeave::getStatus, 0)));
        stats.put("approved", leaveMapper.selectCount(new LambdaQueryWrapper<HrLeave>().eq(HrLeave::getStatus, 1)));
        stats.put("rejected", leaveMapper.selectCount(new LambdaQueryWrapper<HrLeave>().eq(HrLeave::getStatus, 2)));
        return Result.success(stats);
    }

    private String getLeaveTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "annual": return "年假";
            case "sick": return "病假";
            case "personal": return "事假";
            case "marriage": return "婚假";
            case "maternity": return "产假";
            case "paternity": return "陪产假";
            case "bereavement": return "丧假";
            default: return type;
        }
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
