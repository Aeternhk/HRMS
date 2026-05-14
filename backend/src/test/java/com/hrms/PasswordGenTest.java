package com.hrms;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hashed = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        System.out.println("Length: " + hashed.length());
        
        // 验证
        boolean matches = encoder.matches(password, hashed);
        System.out.println("Verify: " + matches);
    }
}
