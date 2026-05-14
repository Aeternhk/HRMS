package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.HrAttendance;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface HrAttendanceMapper extends BaseMapper<HrAttendance> {

    @Select("SELECT * FROM hr_attendance WHERE user_id = #{userId} AND DATE(attendance_date) = #{date} AND shift_type = 'MORNING' AND (deleted IS NULL OR deleted = 0)")
    HrAttendance findMorningByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT * FROM hr_attendance WHERE user_id = #{userId} AND DATE(attendance_date) = #{date} AND (deleted IS NULL OR deleted = 0) ORDER BY FIELD(shift_type, 'MORNING', 'AFTERNOON', 'OVERTIME')")
    List<HrAttendance> findAllByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 查询某天所有打卡记录（不依赖shift_type字段，用于旧表结构兼容） */
    @Select("SELECT * FROM hr_attendance WHERE user_id = #{userId} AND DATE(attendance_date) = #{date} AND (deleted IS NULL OR deleted = 0) ORDER BY id")
    List<HrAttendance> findAllByUserIdAndDateFallback(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 查询某天某时段的打卡记录 */
    @Select("SELECT * FROM hr_attendance WHERE user_id = #{userId} AND DATE(attendance_date) = #{date} AND shift_type = #{shiftType} AND (deleted IS NULL OR deleted = 0)")
    HrAttendance findByUserIdAndDateAndShift(@Param("userId") Long userId,
                                              @Param("date") LocalDate date,
                                              @Param("shiftType") String shiftType);

    @Select("SELECT * FROM hr_attendance WHERE user_id = #{userId} AND DATE_FORMAT(attendance_date, '%Y-%m') = #{month} AND (deleted IS NULL OR deleted = 0) ORDER BY attendance_date DESC, shift_type ASC")
    List<HrAttendance> findByUserIdAndMonth(@Param("userId") Long userId, @Param("month") String month);

    /**
     * 查询某用户某月的考勤统计
     */
    @Select("SELECT " +
            "COUNT(*) AS totalDays, " +
            "SUM(CASE WHEN check_in_status = '正常' AND (check_out_status = '正常' OR check_out_status IS NULL) THEN 1 ELSE 0 END) AS normalDays, " +
            "SUM(CASE WHEN check_in_status = '迟到' THEN 1 ELSE 0 END) AS lateCount, " +
            "SUM(CASE WHEN result = '缺勤' THEN 1 ELSE 0 END) AS absentCount " +
            "FROM hr_attendance " +
            "WHERE user_id = #{userId} AND DATE_FORMAT(attendance_date, '%Y-%m') = #{month} AND (deleted IS NULL OR deleted = 0)")
    Map<String, Object> countMonthStats(@Param("userId") Long userId, @Param("month") String month);

    @Insert("INSERT INTO hr_attendance (user_id, attendance_date, shift_type, check_in_time, check_out_time, " +
            "check_in_status, check_out_status, work_hours, result, create_time, update_time) " +
            "VALUES (#{userId}, #{attendanceDate}, #{shiftType}, #{checkInTime}, #{checkOutTime}, " +
            "#{checkInStatus}, #{checkOutStatus}, #{workHours}, #{result}, #{createTime}, #{updateTime}) " +
            "ON DUPLICATE KEY UPDATE " +
            "check_in_time = VALUES(check_in_time), check_out_time = VALUES(check_out_time), " +
            "check_in_status = VALUES(check_in_status), check_out_status = VALUES(check_out_status), " +
            "work_hours = VALUES(work_hours), result = VALUES(result), update_time = VALUES(update_time)")
    void insertOrUpdate(HrAttendance attendance);
}
