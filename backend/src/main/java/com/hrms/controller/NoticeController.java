package com.hrms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hrms.common.Result;
import com.hrms.entity.SysNotice;
import com.hrms.mapper.SysNoticeMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = "*")
public class NoticeController {

    private final SysNoticeMapper noticeMapper;

    public NoticeController(SysNoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @GetMapping("/list")
    public Result<List<SysNotice>> list() {
        List<SysNotice> list = noticeMapper.selectList(
            new LambdaQueryWrapper<SysNotice>()
                .orderByDesc(SysNotice::getCreateTime)
        );
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<SysNotice> getById(@PathVariable Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        return Result.success(notice);
    }

    @PostMapping
    public Result<Void> create(@RequestBody SysNotice notice) {
        notice.setCreateTime(LocalDateTime.now());
        noticeMapper.insert(notice);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysNotice notice) {
        notice.setId(id);
        noticeMapper.updateById(notice);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        noticeMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        SysNotice notice = noticeMapper.selectById(id);
        notice.setStatus(1);
        notice.setPublishTime(LocalDateTime.now());
        noticeMapper.updateById(notice);
        return Result.success();
    }
}
