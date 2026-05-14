package com.hrms.service;

import com.hrms.entity.SysRole;
import com.hrms.entity.SysUser;
import com.hrms.mapper.SysRoleMapper;
import com.hrms.mapper.SysUserMapper;
import com.hrms.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 后台管理系统登录
     * 支持工号或手机号登录
     * 登录权限完全由 sys_user.login_type 字段控制：
     *   1=仅小程序  2=仅后台管理  3=两者皆可
     */
    public Map<String, Object> login(String account, String password) {
        // 1. 查找用户（支持工号或手机号）
        SysUser user = sysUserMapper.findByUsername(account);
        if (user == null) {
            user = sysUserMapper.findByPhone(account);
        }
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 校验账号状态
        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        // 4. 校验登录权限：login_type 必须为 2(仅后台) 或 3(两者都可)
        Integer loginType = user.getLoginType();
        if (loginType == null || (loginType != 2 && loginType != 3)) {
            throw new RuntimeException("该账号无权登录后台管理系统");
        }

        // 5. 获取角色名称（仅用于展示，不做权限控制）
        String roleName = "未知角色";
        if (user.getRoleId() != null) {
            SysRole role = sysRoleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleName = role.getName();
            }
        }

        // 6. 生成Token + 更新最后登录时间
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), "ROLE_" + user.getRoleId());
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("name", user.getName());
        result.put("role", "ROLE_" + user.getRoleId());
        result.put("roleName", roleName);
        result.put("avatar", user.getAvatar());

        return result;
    }
}
