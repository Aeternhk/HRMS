package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.HrContract;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrContractMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contract")
@CrossOrigin(origins = "*")
public class ContractController {

    private final HrContractMapper contractMapper;
    private final SysUserMapper userMapper;

    public ContractController(HrContractMapper contractMapper, SysUserMapper userMapper) {
        this.contractMapper = contractMapper;
        this.userMapper = userMapper;
    }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String contractType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {

        LambdaQueryWrapper<HrContract> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            // 搜索合同编号或员工姓名
            wrapper.like(HrContract::getContractNo, keyword);
            // 同时搜索员工姓名
            List<SysUser> users = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>().like(SysUser::getName, keyword)
            );
            if (!users.isEmpty() && !keyword.isEmpty()) {
                List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
                wrapper.or().in(HrContract::getUserId, userIds);
            }
        }
        if (contractType != null && !contractType.isEmpty()) {
            wrapper.eq(HrContract::getContractType, contractType);
        }
        if (status != null) {
            wrapper.eq(HrContract::getStatus, status);
        }
        if (userId != null) {
            wrapper.eq(HrContract::getUserId, userId);
        }

        wrapper.orderByDesc(HrContract::getCreateTime);

        Page<HrContract> pageResult = new Page<>(page, pageSize);
        Page<HrContract> result = contractMapper.selectPage(pageResult, wrapper);

        List<Map<String, Object>> listWithUser = result.getRecords().stream().map(contract -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", contract.getId());
            map.put("userId", contract.getUserId());
            map.put("contractType", contract.getContractType());
            map.put("contractTypeName", getContractTypeName(contract.getContractType()));
            map.put("contractNo", contract.getContractNo());
            map.put("startDate", contract.getStartDate());
            map.put("endDate", contract.getEndDate());
            map.put("contractPeriod", contract.getContractPeriod());
            map.put("signDate", contract.getSignDate());
            map.put("status", contract.getStatus());
            map.put("statusName", getStatusName(contract.getStatus()));
            map.put("remark", contract.getRemark());
            map.put("createTime", contract.getCreateTime());

            SysUser user = userMapper.selectById(contract.getUserId());
            if (user != null) {
                map.put("userName", user.getName());
                map.put("userUsername", user.getUsername());
            } else {
                map.put("userName", "未知");
                map.put("userUsername", "");
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
    public Result<HrContract> getById(@PathVariable Long id) {
        return Result.success(contractMapper.selectById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody HrContract contract) {
        contract.setStatus(1); // 默认有效
        contract.setCreateTime(LocalDateTime.now());
        contractMapper.insert(contract);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody HrContract contract) {
        contract.setId(id);
        contractMapper.updateById(contract);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 终止/解除合同
     */
    @PutMapping("/{id}/terminate")
    public Result<Void> terminate(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        HrContract contract = contractMapper.selectById(id);
        if (contract == null) return Result.error("记录不存在");
        
        contract.setStatus(2); // 解除状态
        if (body.get("remark") != null) {
            contract.setRemark((String) body.get("remark"));
        }
        contractMapper.updateById(contract);
        return Result.success();
    }

    /**
     * 合同到期提醒（30天内到期）
     */
    @GetMapping("/expiring")
    public Result<List<Map<String, Object>>> expiring() {
        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        LambdaQueryWrapper<HrContract> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HrContract::getStatus, 1) // 有效合同
               .le(HrContract::getEndDate, thirtyDaysLater)
               .ge(HrContract::getEndDate, LocalDate.now())
               .orderByAsc(HrContract::getEndDate);

        List<HrContract> contracts = contractMapper.selectList(wrapper);

        List<Map<String, Object>> result = contracts.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("userId", c.getUserId());
            map.put("contractNo", c.getContractNo());
            map.put("contractType", c.getContractType());
            map.put("contractTypeName", getContractTypeName(c.getContractType()));
            map.put("endDate", c.getEndDate());
            
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());
            map.put("daysLeft", daysLeft);

            SysUser user = userMapper.selectById(c.getUserId());
            map.put("userName", user != null ? user.getName() : "未知");

            return map;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", contractMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("valid", contractMapper.selectCount(new LambdaQueryWrapper<HrContract>().eq(HrContract::getStatus, 1)));
        stats.put("expired", contractMapper.selectCount(new LambdaQueryWrapper<HrContract>().eq(HrContract::getStatus, 0)));
        stats.put("terminated", contractMapper.selectCount(new LambdaQueryWrapper<HrContract>().eq(HrContract::getStatus, 2)));

        // 30天内到期数量
        LocalDate thirtyDaysLater = LocalDate.now().plusDays(30);
        long expiringSoon = contractMapper.selectCount(new LambdaQueryWrapper<HrContract>()
            .eq(HrContract::getStatus, 1)
            .le(HrContract::getEndDate, thirtyDaysLater)
            .ge(HrContract::getEndDate, LocalDate.now()));
        stats.put("expiringSoon", expiringSoon);

        return Result.success(stats);
    }

    private String getContractTypeName(String type) {
        if (type == null) return "";
        switch (type) {
            case "labor": return "劳动合同";
            case "service": return "劳务合同";
            case "internship": return "实习协议";
            case "confidentiality": return "保密协议";
            case "training": return "培训协议";
            default: return type;
        }
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "已过期";
            case 1: return "有效";
            case 2: return "已解除";
            default: return "未知";
        }
    }
}
