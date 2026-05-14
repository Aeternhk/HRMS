package com.hrms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String dbHash = "$2b$10$qjMu9mbn24SmFoW2AfbYmu0tmkZLucMJYc6XHwHHTSsCEqoHe.Z06";
        System.out.println("密码: " + password);
        System.out.println("数据库哈希: " + dbHash);
        System.out.println("Spring验证结果: " + encoder.matches(password, dbHash));
    }
}
