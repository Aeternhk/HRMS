package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("hr_recruit_interview")
public class HrRecruitInterview {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long candidateId;

    private Long positionId;

    private Integer round; // 1:初面 2:复试 3:终面

    private Long interviewerId; // 面试官ID

    private String interviewType; // onsite/video/phone

    private LocalDateTime interviewDate; // 面试时间

    private Integer durationMinutes;

    private String location; // 地点或链接

    private Integer score; // 0-100

    private String result; // pass/fail/pending/reschedule

    private String feedback;

    private Integer nextRound; // 是否进入下一轮

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
