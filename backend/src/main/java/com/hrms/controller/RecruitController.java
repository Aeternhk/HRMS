package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.*;
import com.hrms.mapper.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recruit")
@CrossOrigin(origins = "*")
public class RecruitController {

    private final HrRecruitPositionMapper positionMapper;
    private final HrRecruitCandidateMapper candidateMapper;
    private final HrRecruitInterviewMapper interviewMapper;
    private final SysUserMapper userMapper;
    private final HrDepartmentMapper departmentMapper;

    public RecruitController(HrRecruitPositionMapper positionMapper,
                            HrRecruitCandidateMapper candidateMapper,
                            HrRecruitInterviewMapper interviewMapper,
                            SysUserMapper userMapper,
                            HrDepartmentMapper departmentMapper) {
        this.positionMapper = positionMapper;
        this.candidateMapper = candidateMapper;
        this.interviewMapper = interviewMapper;
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;
    }

    // ==================== 职位管理 ====================

    @GetMapping("/position/list")
    public Result<Map<String, Object>> positionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<HrRecruitPosition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(HrRecruitPosition::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(HrRecruitPosition::getStatus, status);
        }
        wrapper.orderByDesc(HrRecruitPosition::getCreateTime);

        Page<HrRecruitPosition> pageResult = new Page<>(page, pageSize);
        Page<HrRecruitPosition> result = positionMapper.selectPage(pageResult, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(pos -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", pos.getId());
            map.put("title", pos.getTitle());
            map.put("departmentId", pos.getDepartmentId());
            map.put("recruitCount", pos.getRecruitCount());
            map.put("jobType", pos.getJobType());
            map.put("experience", pos.getExperience());
            map.put("education", pos.getEducation());
            map.put("salaryMin", pos.getSalaryMin());
            map.put("salaryMax", pos.getSalaryMax());
            map.put("location", pos.getLocation());
            map.put("status", pos.getStatus());
            map.put("statusName", getPositionStatusName(pos.getStatus()));
            map.put("publishTime", pos.getPublishTime());
            map.put("deadline", pos.getDeadline());
            // 部门名称
            if (pos.getDepartmentId() != null) {
                HrDepartment dept = departmentMapper.selectById(pos.getDepartmentId());
                map.put("departmentName", dept != null ? dept.getName() : "");
            } else {
                map.put("departmentName", "");
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping("/position")
    public Result<Void> createPosition(@RequestBody HrRecruitPosition pos) {
        pos.setStatus(0); // 默认草稿
        pos.setCreateTime(LocalDateTime.now());
        positionMapper.insert(pos);
        return Result.success();
    }

    @PutMapping("/position/{id}")
    public Result<Void> updatePosition(@PathVariable Long id, @RequestBody HrRecruitPosition pos) {
        pos.setId(id);
        positionMapper.updateById(pos);
        return Result.success();
    }

    @PutMapping("/position/{id}/publish")
    public Result<Void> publishPosition(@PathVariable Long id) {
        HrRecruitPosition pos = positionMapper.selectById(id);
        if (pos == null) return Result.error("职位不存在");
        pos.setStatus(1); // 招聘中
        pos.setPublishTime(LocalDateTime.now());
        positionMapper.updateById(pos);
        return Result.success();
    }

    @DeleteMapping("/position/{id}")
    public Result<Void> deletePosition(@PathVariable Long id) {
        positionMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 候选人/简历管理 ====================

    @GetMapping("/candidate/list")
    public Result<Map<String, Object>> candidateList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long positionId) {

        LambdaQueryWrapper<HrRecruitCandidate> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(HrRecruitCandidate::getName, keyword)
                   .or()
                   .like(HrRecruitCandidate::getPhone, keyword)
                   .or()
                   .like(HrRecruitCandidate::getCurrentPosition, keyword);
        }
        if (status != null) {
            wrapper.eq(HrRecruitCandidate::getStatus, status);
        }
        if (positionId != null) {
            wrapper.eq(HrRecruitCandidate::getPositionId, positionId);
        }
        wrapper.orderByDesc(HrRecruitCandidate::getCreateTime);

        Page<HrRecruitCandidate> pageResult = new Page<>(page, pageSize);
        Page<HrRecruitCandidate> result = candidateMapper.selectPage(pageResult, wrapper);

        List<Map<String, Object>> list = result.getRecords().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("phone", c.getPhone());
            map.put("email", c.getEmail());
            map.put("gender", c.getGender());
            map.put("age", c.getAge());
            map.put("education", c.getEducation());
            map.put("currentPosition", c.getCurrentPosition());
            map.put("currentCompany", c.getCurrentCompany());
            map.put("experienceYears", c.getExperienceYears());
            map.put("expectedSalary", c.getExpectedSalary());
            map.put("positionId", c.getPositionId());
            map.put("resumeFile", c.getResumeFile());
            map.put("status", c.getStatus());
            map.put("remark", c.getRemark());
            map.put("createTime", c.getCreateTime());
            map.put("statusName", getCandidateStatusName(c.getStatus()));
            // 职位名称
            HrRecruitPosition pos = positionMapper.selectById(c.getPositionId());
            map.put("positionTitle", pos != null ? pos.getTitle() : "");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", result.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        return Result.success(data);
    }

    @PostMapping("/candidate")
    public Result<Void> createCandidate(@RequestBody HrRecruitCandidate candidate) {
        candidate.setStatus(0); // 默认待筛选
        candidate.setCreateTime(LocalDateTime.now());
        candidateMapper.insert(candidate);
        return Result.success();
    }

    @PutMapping("/candidate/{id}")
    public Result<Void> updateCandidate(@PathVariable Long id, @RequestBody HrRecruitCandidate c) {
        c.setId(id);
        candidateMapper.updateById(c);
        return Result.success();
    }

    @PutMapping("/candidate/{id}/status")
    public Result<Void> updateCandidateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        HrRecruitCandidate c = candidateMapper.selectById(id);
        if (c == null) return Result.error("候选人不存在");
        c.setStatus((Integer) body.get("status"));
        if (body.get("remark") != null) {
            c.setRemark((String) body.get("remark"));
        }
        candidateMapper.updateById(c);
        return Result.success();
    }

    @DeleteMapping("/candidate/{id}")
    public Result<Void> deleteCandidate(@PathVariable Long id) {
        candidateMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 面试管理 ====================

    @GetMapping("/interview/list")
    public Result<List<Map<String, Object>>> interviewList(@RequestParam(required = false) Long candidateId) {
        LambdaQueryWrapper<HrRecruitInterview> wrapper = new LambdaQueryWrapper<>();
        if (candidateId != null) {
            wrapper.eq(HrRecruitInterview::getCandidateId, candidateId);
        }
        wrapper.orderByAsc(HrRecruitInterview::getInterviewDate);

        List<HrRecruitInterview> interviews = interviewMapper.selectList(wrapper);
        List<Map<String, Object>> list = interviews.stream().map(ivt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", ivt.getId());
            map.put("candidateId", ivt.getCandidateId());
            map.put("positionId", ivt.getPositionId());
            map.put("round", ivt.getRound());
            map.put("interviewerId", ivt.getInterviewerId());
            map.put("interviewType", ivt.getInterviewType());
            map.put("interviewDate", ivt.getInterviewDate());
            map.put("durationMinutes", ivt.getDurationMinutes());
            map.put("location", ivt.getLocation());
            map.put("score", ivt.getScore());
            map.put("result", ivt.getResult());
            map.put("feedback", ivt.getFeedback());
            map.put("nextRound", ivt.getNextRound());

            // 候选人信息
            HrRecruitCandidate cand = candidateMapper.selectById(ivt.getCandidateId());
            if (cand != null) {
                map.put("candidateName", cand.getName());
            }
            // 面试官信息
            if (ivt.getInterviewerId() != null) {
                SysUser interviewer = userMapper.selectById(ivt.getInterviewerId());
                map.put("interviewerName", interviewer != null ? interviewer.getName() : "");
            }
            // 轮次名称
            map.put("roundName", getRoundName(ivt.getRound()));
            return map;
        }).collect(Collectors.toList());

        return Result.success(list);
    }

    @PostMapping("/interview")
    public Result<Void> createInterview(@RequestBody HrRecruitInterview ivt) {
        ivt.setCreateTime(LocalDateTime.now());
        interviewMapper.insert(ivt);
        return Result.success();
    }

    @PutMapping("/interview/{id}")
    public Result<Void> updateInterview(@PathVariable Long id, @RequestBody HrRecruitInterview ivt) {
        ivt.setId(id);
        interviewMapper.updateById(ivt);
        
        // 如果面试通过且需要下一轮，自动更新候选人状态为面试中
        if ("pass".equals(ivt.getResult()) && ivt.getNextRound() == 1) {
            HrRecruitCandidate cand = candidateMapper.selectById(ivt.getCandidateId());
            if (cand != null) {
                cand.setStatus(2); // 面试中
                candidateMapper.updateById(cand);
            }
        }
        return Result.success();
    }

    @DeleteMapping("/interview/{id}")
    public Result<Void> deleteInterview(@PathVariable Long id) {
        interviewMapper.deleteById(id);
        return Result.success();
    }

    // ==================== 统计 ====================

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("openPositions", positionMapper.selectCount(new LambdaQueryWrapper<HrRecruitPosition>().eq(HrRecruitPosition::getStatus, 1)));
        stats.put("totalCandidates", candidateMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.put("pendingCandidates", candidateMapper.selectCount(new LambdaQueryWrapper<HrRecruitCandidate>().eq(HrRecruitCandidate::getStatus, 0)));
        stats.put("interviewing", candidateMapper.selectCount(new LambdaQueryWrapper<HrRecruitCandidate>().eq(HrRecruitCandidate::getStatus, 2)));
        stats.put("hired", candidateMapper.selectCount(new LambdaQueryWrapper<HrRecruitCandidate>().eq(HrRecruitCandidate::getStatus, 3)));
        stats.put("todayInterviews", interviewMapper.selectCount(new LambdaQueryWrapper<HrRecruitInterview>()
            .ge(HrRecruitInterview::getInterviewDate, LocalDateTime.now().with(java.time.LocalTime.MIN))));
        return Result.success(stats);
    }

    // ==================== 辅助方法 ====================

    private String getPositionStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "草稿";
            case 1: return "招聘中";
            case 2: return "已暂停";
            case 3: return "已结束";
            default: return "";
        }
    }

    private String getCandidateStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待筛选";
            case 1: return "初筛通过";
            case 2: return "面试中";
            case 3: return "已录用";
            case 4: return "已淘汰";
            case 5: return "已放弃";
            default: return "";
        }
    }

    private String getRoundName(Integer round) {
        if (round == null) return "";
        switch (round) {
            case 1: return "初面";
            case 2: return "复试";
            case 3: return "终面";
            default: return round + "轮";
        }
    }
}
