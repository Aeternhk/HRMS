package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_contract")
public class HrContract {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String contractType;

    private String contractNo;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer contractPeriod; // 合同期限(月)

    private LocalDate signDate; // 签订日期

    private String contractFile; // 合同文件路径

    private Integer status; // 1:有效 0:过期 2:解除

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
