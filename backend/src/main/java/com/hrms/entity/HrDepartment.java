package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("hr_department")
public class HrDepartment {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private String code;
    
    private Long parentId;
    
    private Integer level;
    
    private String path;
    
    private Long leaderId;
    
    private Integer sort;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    /** 子部门列表（非数据库字段，用于树形API返回） */
    @TableField(exist = false)
    private List<HrDepartment> children = new ArrayList<>();
}
