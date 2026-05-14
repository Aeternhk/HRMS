package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.VoteSubject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VoteSubjectMapper extends BaseMapper<VoteSubject> {

    /**
     * 进行中的投票（status=1 且在有效期内）
     */
    @Select("SELECT * FROM vote_subject WHERE deleted = 0 AND status = 1 AND start_time <= NOW() AND end_time > NOW() ORDER BY start_time DESC")
    List<VoteSubject> selectActiveList();

    /**
     * 已结束的投票（end_time <= NOW() 或 status = 2）
     */
    @Select("SELECT * FROM vote_subject WHERE deleted = 0 AND (end_time <= NOW() OR status = 2) ORDER BY end_time DESC")
    List<VoteSubject> selectEndedList();

    /**
     * 统计指定用户尚未投票的进行中投票数量
     */
    @Select("SELECT COUNT(*) FROM vote_subject vs " +
            "WHERE vs.deleted = 0 " +
            "AND vs.status = 1 " +
            "AND vs.start_time <= NOW() AND vs.end_time > NOW() " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM vote_record vr WHERE vr.subject_id = vs.id AND vr.voter_id = #{userId}" +
            ")")
    int countPendingVotes(@Param("userId") Long userId);
}

