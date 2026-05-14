package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_salary")
public class HrSalary {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String salaryMonth;

    private BigDecimal baseSalary;

    private BigDecimal positionAllowance;

    private BigDecimal performance;

    private BigDecimal overtimePay;

    private BigDecimal bonus;

    private BigDecimal mealAllowance;

    private BigDecimal transportAllowance;

    private BigDecimal socialInsurance;

    private BigDecimal housingFund;

    private BigDecimal otherDeduction;

    private BigDecimal netSalary;

    private Integer status;

    private LocalDate grantDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
