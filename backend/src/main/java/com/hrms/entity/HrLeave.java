package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_leave")
public class HrLeave {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String leaveType;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal days;

    private String reason;

    private Integer status;

    private Long approverId;

    private LocalDateTime approveTime;

    private String approveRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
