package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vote_subject")
public class VoteSubject {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Integer voteType;

    private Integer maxSelections;

    private Integer anonymous;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer targetType;

    private String targetIds;

    private Integer status;

    private Long creatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
