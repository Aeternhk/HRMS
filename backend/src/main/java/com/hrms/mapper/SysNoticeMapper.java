package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.SysNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    /**
     * 获取最新已发布公告（最多 n 条，按发布时间倒序）
     */
    @Select("SELECT * FROM sys_notice WHERE status = 1 AND deleted = 0 ORDER BY publish_time DESC LIMIT #{limit}")
    List<SysNotice> selectLatest(int limit);

    /**
     * 获取当月已发布的公告（按发布时间倒序）
     */
    @Select("SELECT * FROM sys_notice WHERE status = 1 AND deleted = 0 AND DATE_FORMAT(publish_time, '%Y-%m') = #{month} ORDER BY publish_time DESC LIMIT #{limit}")
    List<SysNotice> selectByMonth(@Param("month") String month, @Param("limit") int limit);
}
