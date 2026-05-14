package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.HrOvertime;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HrOvertimeMapper extends BaseMapper<HrOvertime> {

    @Select("SELECT * FROM hr_overtime WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<HrOvertime> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM hr_overtime WHERE user_id = #{userId} AND status = 0 AND deleted = 0")
    int countPendingByUserId(@Param("userId") Long userId);
}
