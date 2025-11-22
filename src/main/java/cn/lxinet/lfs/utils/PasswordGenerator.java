package cn.lxinet.lfs.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成BCrypt密码哈希
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password1 = "admin123";
        String password2 = "user123";
        
        String hash1 = encoder.encode(password1);
        String hash2 = encoder.encode(password2);
        
        System.out.println("admin123 的BCrypt哈希:");
        System.out.println(hash1);
        System.out.println();
        System.out.println("user123 的BCrypt哈希:");
        System.out.println(hash2);
        System.out.println();
        
        // 验证
        System.out.println("验证 admin123: " + encoder.matches(password1, hash1));
        System.out.println("验证 user123: " + encoder.matches(password2, hash2));
    }
}

