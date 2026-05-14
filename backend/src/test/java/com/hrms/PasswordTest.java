package com.hrms;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordTest {
    
    @Test
    public void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hashed = encoder.encode(password);
        
        System.out.println("========================================");
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        System.out.println("Length: " + hashed.length());
        System.out.println("========================================");
        
        assertTrue(encoder.matches(password, hashed));
    }
    
    @Test
    public void verifyOldHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String oldHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO";
        System.out.println("Old hash length: " + oldHash.length());
        System.out.println("Verify result: " + encoder.matches("123456", oldHash));
    }
}
