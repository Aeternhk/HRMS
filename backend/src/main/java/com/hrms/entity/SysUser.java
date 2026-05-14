package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    // ==================== 认证字段 ====================
    private String username;  // 工号，如 HR001、T001

    private String password;

    private Long roleId;

    private Integer loginType;

    private Integer status;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    // ==================== 员工基本信息字段 ====================
    private String name;

    private String gender;

    private String nation;

    private String idCard;

    private LocalDate birthday;

    private String maritalStatus;

    private String phone;

    private String emergencyContact;

    private String emergencyPhone;

    private String nativePlace;

    private String address;

    private String email;

    private String education;

    private String graduateSchool;

    private String major;

    private LocalDate entryDate;

    private LocalDate positiveDate;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;

    private Long departmentId;

    private String position;

    @TableField("`rank`")
    private String rank;

    private Integer employeeStatus;

    private BigDecimal baseSalary;

    private String photo;

    private String avatar;

    // ==================== 审计字段 ====================
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
