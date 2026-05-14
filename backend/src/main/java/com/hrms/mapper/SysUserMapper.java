package com.hrms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hrms.entity.SysRole;
import com.hrms.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    SysUser findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0")
    SysUser findByPhone(@Param("phone") String phone);

    @Select("SELECT * FROM sys_role WHERE id = #{roleId} AND deleted = 0")
    SysRole selectRoleById(@Param("roleId") Long roleId);
}
