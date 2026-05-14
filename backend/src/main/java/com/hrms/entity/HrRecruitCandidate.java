package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hr_recruit_candidate")
public class HrRecruitCandidate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long positionId; // 应聘职位ID

    private String name;

    private String gender;

    private String phone;

    private String email;

    private Integer age;

    private String education;

    private String school;

    private String major;

    private Integer experienceYears; // 工作年限

    private String currentCompany;

    private String currentPosition;

    private BigDecimal expectedSalary;

    private String resumeFile;

    private String source; // 来源: website/referral/headhunter/campus

    private String selfIntro; // 自我介绍

    private Integer status; // 0:待筛选 1:初筛通过 2:面试中 3:已录用 4:已淘汰 5:已放弃

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
