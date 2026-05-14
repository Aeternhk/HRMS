package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String operation; // 操作描述

    private String method; // 方法名

    private String params; // 参数

    private String ip; // IP地址

    private LocalDateTime createTime;
}
