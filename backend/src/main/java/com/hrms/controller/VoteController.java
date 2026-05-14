package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hrms.common.Result;
import com.hrms.entity.VoteOption;
import com.hrms.entity.VoteRecord;
import com.hrms.entity.VoteSubject;
import com.hrms.mapper.VoteOptionMapper;
import com.hrms.mapper.VoteRecordMapper;
import com.hrms.mapper.VoteSubjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 投票管理接口（后台管理端）
 * 区别于 MiniProgramController 中的小程序端投票接口
 */
@RestController
@RequestMapping("/api/vote")
@CrossOrigin(origins = "*")
public class VoteController {

    private final VoteSubjectMapper voteSubjectMapper;
    private final VoteOptionMapper voteOptionMapper;
    private final VoteRecordMapper voteRecordMapper;

    public VoteController(VoteSubjectMapper voteSubjectMapper,
                         VoteOptionMapper voteOptionMapper,
                         VoteRecordMapper voteRecordMapper) {
        this.voteSubjectMapper = voteSubjectMapper;
        this.voteOptionMapper = voteOptionMapper;
        this.voteRecordMapper = voteRecordMapper;
    }

    /**
     * 分页查询投票列表
     * GET /api/vote/page?keyword=&status=&page=1&pageSize=10
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<VoteSubject> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<VoteSubject> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(VoteSubject::getTitle, keyword.trim());
        }
        if (status != null) {
            wrapper.eq(VoteSubject::getStatus, status);
        }
        wrapper.orderByDesc(VoteSubject::getCreateTime);

        IPage<VoteSubject> pageResult = voteSubjectMapper.selectPage(pageParam, wrapper);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] typeKeys = {"", "SINGLE", "MULTI", "SCORE", "RANK"};
        String[] typeNames = {"", "单选", "多选", "评分", "排序"};

        List<Map<String, Object>> records = new ArrayList<>();
        for (VoteSubject subject : pageResult.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", subject.getId());
            item.put("title", subject.getTitle());
            item.put("description", subject.getDescription());

            int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;
            item.put("type", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
            item.put("typeName", vt < typeNames.length ? typeNames[vt] : "单选");

            item.put("anonymous", subject.getAnonymous());
            item.put("startTime", subject.getStartTime() != null ? subject.getStartTime().format(fmt) : null);
            item.put("endTime", subject.getEndTime() != null ? subject.getEndTime().format(fmt) : null);
            item.put("status", subject.getStatus());

            // 状态文本
            int st = subject.getStatus() != null ? subject.getStatus() : 0;
            if (st == 0) item.put("statusText", "未开始");
            else if (st == 1) item.put("statusText", "进行中");
            else item.put("statusText", "已结束");

            // 统计参与人数
            int participantCount = voteRecordMapper.countBySubjectId(subject.getId());
            item.put("participantCount", participantCount);
            // 选项数量
            List<VoteOption> options = voteOptionMapper.findBySubjectId(subject.getId());
            item.put("optionCount", options.size());

            item.put("createTime", subject.getCreateTime() != null ? subject.getCreateTime().format(fmt) : null);
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
     * 获取投票详情（含选项列表）
     * GET /api/vote/{id}
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        VoteSubject subject = voteSubjectMapper.selectById(id);
        if (subject == null || subject.getDeleted() != null && subject.getDeleted() == 1) {
            return Result.error("投票不存在");
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] typeKeys = {"", "SINGLE", "MULTI", "SCORE", "RANK"};
        String[] typeNames = {"", "单选", "多选", "评分", "排序"};

        Map<String, Object> result = new HashMap<>();
        result.put("id", subject.getId());
        result.put("title", subject.getTitle());
        result.put("description", subject.getDescription());

        int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;
        result.put("voteType", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
        result.put("typeName", vt < typeNames.length ? typeNames[vt] : "单选");

        result.put("maxSelections", subject.getMaxSelections());
        result.put("anonymous", subject.getAnonymous());
        result.put("startTime", subject.getStartTime() != null ? subject.getStartTime().format(fmt) : null);
        result.put("endTime", subject.getEndTime() != null ? subject.getEndTime().format(fmt) : null);
        result.put("targetType", subject.getTargetType());
        result.put("targetIds", subject.getTargetIds());
        result.put("status", subject.getStatus());

        List<VoteOption> options = voteOptionMapper.findBySubjectId(id);
        List<Map<String, Object>> optionList = new ArrayList<>();
        for (VoteOption opt : options) {
            Map<String, Object> o = new HashMap<>();
            o.put("id", opt.getId());
            o.put("content", opt.getContent());
            o.put("sort", opt.getSort());
            optionList.add(o);
        }
        result.put("options", optionList);

        return Result.success(result);
    }

    /**
     * 创建投票
     * POST /api/vote
     */
    @PostMapping
    public Result<Void> create(@RequestBody Map<String, Object> params) {
        try {
            VoteSubject subject = new VoteSubject();
            subject.setTitle((String) params.get("title"));
            subject.setDescription((String) params.getOrDefault("description", ""));

            Integer voteType = params.get("voteType") != null ? Integer.valueOf(params.get("voteType").toString()) : 1;
            subject.setVoteType(voteType);
            subject.setMaxSelections(params.get("maxSelections") != null ? Integer.valueOf(params.get("maxSelections").toString()) : 1);
            subject.setAnonymous(params.get("anonymous") != null ? Integer.valueOf(params.get("anonymous").toString()) : 0);

            if (params.get("startTime") != null && !params.get("startTime").toString().isEmpty()) {
                subject.setStartTime(LocalDateTime.parse((String) params.get("startTime")));
            }
            if (params.get("endTime") != null && !params.get("endTime").toString().isEmpty()) {
                subject.setEndTime(LocalDateTime.parse((String) params.get("endTime")));
            }
            subject.setTargetType(params.get("targetType") != null ? Integer.valueOf(params.get("targetType").toString()) : 1);
            subject.setTargetIds((String) params.getOrDefault("targetIds", null));
            subject.setStatus(0); // 默认未开始

            voteSubjectMapper.insert(subject);

            // 插入选项
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = (List<Map<String, Object>>) params.get("options");
            if (options != null && !options.isEmpty()) {
                for (int i = 0; i < options.size(); i++) {
                    Map<String, Object> optData = options.get(i);
                    VoteOption option = new VoteOption();
                    option.setSubjectId(subject.getId());
                    option.setContent((String) optData.get("content"));
                    option.setSort(i + 1);
                    voteOptionMapper.insert(option);
                }
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新投票（仅基本信息和选项）
     * PUT /api/vote/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        VoteSubject subject = voteSubjectMapper.selectById(id);
        if (subject == null) {
            return Result.error("投票不存在");
        }

        try {
            if (params.containsKey("title")) {
                subject.setTitle((String) params.get("title"));
            }
            if (params.containsKey("description")) {
                subject.setDescription((String) params.get("description"));
            }
            if (params.containsKey("voteType")) {
                subject.setVoteType(Integer.valueOf(params.get("voteType").toString()));
            }
            if (params.containsKey("maxSelections")) {
                subject.setMaxSelections(Integer.valueOf(params.get("maxSelections").toString()));
            }
            if (params.containsKey("anonymous")) {
                subject.setAnonymous(Integer.valueOf(params.get("anonymous").toString()));
            }
            if (params.containsKey("startTime")) {
                String s = (String) params.get("startTime");
                if (!s.isEmpty()) subject.setStartTime(LocalDateTime.parse(s));
            }
            if (params.containsKey("endTime")) {
                String e = (String) params.get("endTime");
                if (!e.isEmpty()) subject.setEndTime(LocalDateTime.parse(e));
            }

            voteSubjectMapper.updateById(subject);

            // 更新选项：删除旧的，重新插入
            LambdaQueryWrapper<VoteOption> delWrapper = new LambdaQueryWrapper<>();
            delWrapper.eq(VoteOption::getSubjectId, id);
            voteOptionMapper.delete(delWrapper);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> options = (List<Map<String, Object>>) params.get("options");
            if (options != null && !options.isEmpty()) {
                for (int i = 0; i < options.size(); i++) {
                    Map<String, Object> optData = options.get(i);
                    VoteOption option = new VoteOption();
                    option.setSubjectId(id);
                    option.setContent((String) optData.get("content"));
                    option.setSort(i + 1);
                    voteOptionMapper.insert(option);
                }
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除投票
     * DELETE /api/vote/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        VoteSubject subject = voteSubjectMapper.selectById(id);
        if (subject == null) {
            return Result.error("投票不存在");
        }
        voteSubjectMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 结束投票
     * POST /api/vote/end/{id}
     */
    @PostMapping("/end/{id}")
    public Result<Void> endVote(@PathVariable Long id) {
        VoteSubject subject = voteSubjectMapper.selectById(id);
        if (subject == null) {
            return Result.error("投票不存在");
        }
        subject.setStatus(2); // 已结束
        voteSubjectMapper.updateById(subject);
        return Result.success();
    }

    /**
     * 获取投票结果（含各选项票数）
     * GET /api/vote/result/{id}
     */
    @GetMapping("/result/{id}")
    public Result<Map<String, Object>> result(@PathVariable Long id) {
        VoteSubject subject = voteSubjectMapper.selectById(id);
        if (subject == null) {
            return Result.error("投票不存在");
        }

        List<VoteOption> options = voteOptionMapper.findBySubjectId(id);
        int totalVotes = voteRecordMapper.countBySubjectId(id);

        List<Map<String, Object>> optionResults = new ArrayList<>();
        for (VoteOption option : options) {
            Map<String, Object> optResult = new HashMap<>();
            optResult.put("id", option.getId());
            optResult.put("content", option.getContent());
            int voteCount = voteRecordMapper.countByOptionId(option.getId());
            optResult.put("voteCount", voteCount);
            optResult.put("percentage", totalVotes > 0 ? Math.round((float) voteCount / totalVotes * 100) : 0);
            optionResults.add(optResult);
        }

        optionResults.sort((a, b) -> (Integer) b.get("voteCount") - (Integer) a.get("voteCount"));

        String[] typeKeys = {"", "SINGLE", "MULTI"};
        int vt = subject.getVoteType() != null ? subject.getVoteType() : 1;

        Map<String, Object> result = new HashMap<>();
        result.put("id", subject.getId());
        result.put("title", subject.getTitle());
        result.put("voteType", vt < typeKeys.length ? typeKeys[vt] : "SINGLE");
        result.put("totalVotes", totalVotes);
        result.put("participantCount", totalVotes);
        result.put("options", optionResults);

        return Result.success(result);
    }
}
