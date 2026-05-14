package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.VoteOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoteOptionMapper extends BaseMapper<VoteOption> {

    @Select("SELECT * FROM vote_option WHERE subject_id = #{subjectId} ORDER BY sort")
    List<VoteOption> findBySubjectId(@Param("subjectId") Long subjectId);
}
