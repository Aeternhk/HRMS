package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("vote_record")
public class VoteRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long subjectId;

    private Long voterId;

    private String optionIds;

    private Integer score;

    private LocalDateTime voteTime;
}
