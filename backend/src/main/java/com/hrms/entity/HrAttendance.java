package com.hrms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("hr_attendance")
public class HrAttendance {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate attendanceDate;

    /** 打卡时段: MORNING=上午, AFTERNOON=下午, OVERTIME=加班 */
    private String shiftType;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String checkInStatus;

    private String checkOutStatus;

    private BigDecimal workHours;

    private String result;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
