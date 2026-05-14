package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.HrDepartment;
import com.hrms.entity.HrSalary;
import com.hrms.entity.SysUser;
import com.hrms.mapper.HrDepartmentMapper;
import com.hrms.mapper.HrSalaryMapper;
import com.hrms.mapper.SysUserMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 薪资管理接口（后台管理端）
 */
@RestController
@RequestMapping("/api/salary")
@CrossOrigin(origins = "*")
public class SalaryController {

    private final HrSalaryMapper hrSalaryMapper;
    private final SysUserMapper sysUserMapper;
    private final HrDepartmentMapper hrDepartmentMapper;

    public SalaryController(HrSalaryMapper hrSalaryMapper, SysUserMapper sysUserMapper, HrDepartmentMapper hrDepartmentMapper) {
        this.hrSalaryMapper = hrSalaryMapper;
        this.sysUserMapper = sysUserMapper;
        this.hrDepartmentMapper = hrDepartmentMapper;
    }

    /**
     * 分页查询薪资列表
     * GET /api/salary/page?keyword=&departmentId=&month=&status=&page=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<HrSalary> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<HrSalary> wrapper = new LambdaQueryWrapper<>();

        // 关键字搜索（按姓名/工号）
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 先查用户ID列表
            List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                    .and(w -> w.like(SysUser::getName, keyword)
                              .or().like(SysUser::getUsername, keyword))
                    .eq(SysUser::getStatus, 1)
                    .eq(SysUser::getDeleted, 0)
            );
            if (!users.isEmpty()) {
                List<Long> userIds = new ArrayList<>();
                for (SysUser u : users) userIds.add(u.getId());
                wrapper.in(HrSalary::getUserId, userIds);
            } else {
                // 无匹配用户，返回空结果
                Map<String, Object> result = new HashMap<>();
                result.put("records", new ArrayList<>());
                result.put("total", 0L);
                result.put("page", page);
                result.put("pageSize", pageSize);
                return Result.success(result);
            }
        }

        // 部门筛选：先查部门下所有用户
        if (departmentId != null && departmentId > 0) {
            List<Long> deptUserIds = getDeptUserIds(departmentId);
            if (!deptUserIds.isEmpty()) {
                wrapper.and(w -> w.in(HrSalary::getUserId, deptUserIds));
            }
        }

        // 月份筛选
        if (month != null && !month.trim().isEmpty()) {
            wrapper.eq(HrSalary::getSalaryMonth, month.trim());
        }

        // 发放状态
        if (status != null) {
            wrapper.eq(HrSalary::getStatus, status);
        }

        wrapper.orderByDesc(HrSalary::getSalaryMonth);

        IPage<HrSalary> pageResult = hrSalaryMapper.selectPage(pageParam, wrapper);

        // 组装带用户信息的返回数据
        List<Map<String, Object>> records = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (HrSalary salary : pageResult.getRecords()) {
            SysUser user = sysUserMapper.selectById(salary.getUserId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", salary.getId());
            item.put("userId", salary.getUserId());

            if (user != null) {
                item.put("employeeNo", user.getUsername());
                item.put("name", user.getName());
                item.put("department", getDepartmentName(user.getDepartmentId()));
                item.put("departmentId", user.getDepartmentId());
            }

            item.put("month", salary.getSalaryMonth());
            item.put("baseSalary", safeGet(salary.getBaseSalary()));
            item.put("positionAllowance", safeGet(salary.getPositionAllowance()));
            item.put("performance", safeGet(salary.getPerformance()));
            item.put("overtimePay", safeGet(salary.getOvertimePay()));
            item.put("bonus", safeGet(salary.getBonus()));
            item.put("mealAllowance", safeGet(salary.getMealAllowance()));
            item.put("transportAllowance", safeGet(salary.getTransportAllowance()));
            item.put("socialInsurance", safeGet(salary.getSocialInsurance()));
            item.put("housingFund", safeGet(salary.getHousingFund()));
            item.put("otherDeduction", safeGet(salary.getOtherDeduction()));

            // 自动计算实发工资（如果未设置）
            BigDecimal netSalary = salary.getNetSalary();
            if (netSalary == null || netSalary.compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal totalIn = zeroIfNull(salary.getBaseSalary())
                        .add(zeroIfNull(salary.getPositionAllowance()))
                        .add(zeroIfNull(salary.getPerformance()))
                        .add(zeroIfNull(salary.getOvertimePay()))
                        .add(zeroIfNull(salary.getBonus()))
                        .add(zeroIfNull(salary.getMealAllowance()))
                        .add(zeroIfNull(salary.getTransportAllowance()));
                BigDecimal totalOut = zeroIfNull(salary.getSocialInsurance())
                        .add(zeroIfNull(salary.getHousingFund()))
                        .add(zeroIfNull(salary.getOtherDeduction()));
                netSalary = totalIn.subtract(totalOut);
            }
            item.put("netSalary", netSalary);

            item.put("status", salary.getStatus() != null ? salary.getStatus() : 0);
            item.put("statusText", salary.getStatus() != null && salary.getStatus() == 1 ? "已发放" : "未发放");
            item.put("grantDate", salary.getGrantDate() != null ? salary.getGrantDate().format(fmt) : null);
            item.put("createTime", salary.getCreateTime() != null ? salary.getCreateTime().toString() : null);

            records.add(item);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    /**
     * 获取薪资详情
     * GET /api/salary/{id}
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        HrSalary salary = hrSalaryMapper.selectById(id);
        if (salary == null || salary.getDeleted() != null && salary.getDeleted() == 1) {
            return Result.error("薪资记录不存在");
        }

        SysUser user = sysUserMapper.selectById(salary.getUserId());
        Map<String, Object> item = new HashMap<>();
        item.put("id", salary.getId());
        item.put("userId", salary.getUserId());
        if (user != null) {
            item.put("employeeNo", user.getUsername());
            item.put("name", user.getName());
            item.put("department", getDepartmentName(user.getDepartmentId()));
        }
        item.put("month", salary.getSalaryMonth());
        item.put("baseSalary", salary.getBaseSalary());
        item.put("positionAllowance", salary.getPositionAllowance());
        item.put("performance", salary.getPerformance());
        item.put("overtimePay", salary.getOvertimePay());
        item.put("bonus", salary.getBonus());
        item.put("mealAllowance", salary.getMealAllowance());
        item.put("transportAllowance", salary.getTransportAllowance());
        item.put("socialInsurance", salary.getSocialInsurance());
        item.put("housingFund", salary.getHousingFund());
        item.put("otherDeduction", salary.getOtherDeduction());
        item.put("netSalary", salary.getNetSalary());
        item.put("status", salary.getStatus());
        item.put("grantDate", salary.getGrantDate());

        return Result.success(item);
    }

    /**
     * 新建薪资记录
     * POST /api/salary
     */
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String month = (String) params.get("month");

            // 检查是否已有同月记录
            LambdaQueryWrapper<HrSalary> existCheck = new LambdaQueryWrapper<>();
            existCheck.eq(HrSalary::getUserId, userId).eq(HrSalary::getSalaryMonth, month);
            if (hrSalaryMapper.selectCount(existCheck) > 0) {
                return Result.error("该员工" + month + "的薪资记录已存在");
            }

            HrSalary salary = new HrSalary();
            salary.setUserId(userId);
            salary.setSalaryMonth(month);
            salary.setBaseSalary(parseBigDecimal(params.get("baseSalary")));
            salary.setPositionAllowance(parseBigDecimal(params.get("positionAllowance")));
            salary.setPerformance(parseBigDecimal(params.get("performance")));
            salary.setOvertimePay(parseBigDecimal(params.get("overtimePay")));
            salary.setBonus(parseBigDecimal(params.get("bonus")));
            salary.setMealAllowance(parseBigDecimal(params.get("mealAllowance")));
            salary.setTransportAllowance(parseBigDecimal(params.get("transportAllowance")));
            salary.setSocialInsurance(parseBigDecimal(params.get("socialInsurance")));
            salary.setHousingFund(parseBigDecimal(params.get("housingFund")));
            salary.setOtherDeduction(parseBigDecimal(params.get("otherDeduction")));
            salary.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 0);

            if (params.get("grantDate") != null && !params.get("grantDate").toString().isEmpty()) {
                salary.setGrantDate(LocalDate.parse(params.get("grantDate").toString()));
            }

            hrSalaryMapper.insert(salary);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新薪资记录
     * PUT /api/salary/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        HrSalary salary = hrSalaryMapper.selectById(id);
        if (salary == null) {
            return Result.error("薪资记录不存在");
        }

        try {
            if (params.containsKey("userId")) {
                salary.setUserId(Long.valueOf(params.get("userId").toString()));
            }
            if (params.containsKey("month")) {
                salary.setSalaryMonth((String) params.get("month"));
            }
            if (params.containsKey("baseSalary")) {
                salary.setBaseSalary(parseBigDecimal(params.get("baseSalary")));
            }
            if (params.containsKey("positionAllowance")) {
                salary.setPositionAllowance(parseBigDecimal(params.get("positionAllowance")));
            }
            if (params.containsKey("performance")) {
                salary.setPerformance(parseBigDecimal(params.get("performance")));
            }
            if (params.containsKey("overtimePay")) {
                salary.setOvertimePay(parseBigDecimal(params.get("overtimePay")));
            }
            if (params.containsKey("bonus")) {
                salary.setBonus(parseBigDecimal(params.get("bonus")));
            }
            if (params.containsKey("mealAllowance")) {
                salary.setMealAllowance(parseBigDecimal(params.get("mealAllowance")));
            }
            if (params.containsKey("transportAllowance")) {
                salary.setTransportAllowance(parseBigDecimal(params.get("transportAllowance")));
            }
            if (params.containsKey("socialInsurance")) {
                salary.setSocialInsurance(parseBigDecimal(params.get("socialInsurance")));
            }
            if (params.containsKey("housingFund")) {
                salary.setHousingFund(parseBigDecimal(params.get("housingFund")));
            }
            if (params.containsKey("otherDeduction")) {
                salary.setOtherDeduction(parseBigDecimal(params.get("otherDeduction")));
            }
            if (params.containsKey("netSalary")) {
                salary.setNetSalary(parseBigDecimal(params.get("netSalary")));
            }
            if (params.containsKey("status")) {
                salary.setStatus(Integer.valueOf(params.get("status").toString()));
                if (salary.getStatus() == 1 && salary.getGrantDate() == null) {
                    salary.setGrantDate(LocalDate.now());
                }
            }
            if (params.containsKey("grantDate")) {
                String gd = params.get("grantDate").toString();
                if (!gd.isEmpty()) {
                    salary.setGrantDate(LocalDate.parse(gd));
                }
            }

            hrSalaryMapper.updateById(salary);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除薪资记录
     * DELETE /api/salary/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        HrSalary salary = hrSalaryMapper.selectById(id);
        if (salary == null) {
            return Result.error("薪资记录不存在");
        }
        hrSalaryMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 标记为已发放（批量）
     * POST /api/salary/grant
     */
    @PostMapping("/grant")
    public Result<Void> grantBatch(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            HrSalary salary = hrSalaryMapper.selectById(id);
            if (salary != null) {
                salary.setStatus(1);
                salary.setGrantDate(LocalDate.now());
                hrSalaryMapper.updateById(salary);
            }
        }
        return Result.success();
    }

    // ==================== 辅助方法 ====================

    private List<Long> getDeptUserIds(Long departmentId) {
        List<SysUser> users = sysUserMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getDepartmentId, departmentId)
                .eq(SysUser::getStatus, 1)
                .eq(SysUser::getDeleted, 0)
        );
        List<Long> ids = new ArrayList<>();
        for (SysUser u : users) ids.add(u.getId());
        return ids;
    }

    private String getDepartmentName(Long deptId) {
        if (deptId == null) return "";
        try {
            HrDepartment dept = hrDepartmentMapper.selectById(deptId);
            return dept != null ? dept.getName() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static BigDecimal zeroIfNull(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private static BigDecimal parseBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try {
            return new BigDecimal(val.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal safeGet(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }
}
