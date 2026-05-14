package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.HrSalary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HrSalaryMapper extends BaseMapper<HrSalary> {

    @Select("SELECT * FROM hr_salary WHERE user_id = #{userId} AND deleted = 0 ORDER BY salary_month DESC LIMIT 1")
    HrSalary findLatestByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM hr_salary WHERE user_id = #{userId} AND deleted = 0 ORDER BY salary_month DESC")
    List<HrSalary> findByUserId(@Param("userId") Long userId);
}
