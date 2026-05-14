package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_recruit_position")
public class HrRecruitPosition {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private Long departmentId;

    private Integer recruitCount; // 招聘人数

    private String jobType; // 全职/兼职/实习

    private String experience; // 经验要求

    private String education; // 学历要求

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String location;

    private String description;

    private String requirements;

    private String benefits;

    private Integer status; // 0:草稿 1:招聘中 2:已暂停 3:已结束

    private Long publisherId;

    private LocalDateTime publishTime;

    private LocalDate deadline;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
