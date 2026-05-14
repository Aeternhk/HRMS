package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vote_option")
public class VoteOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private String content;

    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
