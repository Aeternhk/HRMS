package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.VoteRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VoteRecordMapper extends BaseMapper<VoteRecord> {

    @Select("SELECT COUNT(*) FROM vote_record WHERE subject_id = #{subjectId} AND voter_id = #{voterId}")
    int countBySubjectAndVoter(@Param("subjectId") Long subjectId, @Param("voterId") Long voterId);

    default boolean hasVoted(Long subjectId, Long voterId) {
        return countBySubjectAndVoter(subjectId, voterId) > 0;
    }

    @Select("SELECT COUNT(*) FROM vote_record WHERE FIND_IN_SET(#{optionId}, option_ids)")
    int countByOptionId(@Param("optionId") Long optionId);

    /**
     * 统计某投票主题的参与人数
     */
    @Select("SELECT COUNT(*) FROM vote_record WHERE subject_id = #{subjectId}")
    int countBySubjectId(@Param("subjectId") Long subjectId);
}
