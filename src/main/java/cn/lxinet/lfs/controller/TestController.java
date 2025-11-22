package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.vo.Result;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * 用于生成和验证BCrypt密码
 * 仅用于开发测试，生产环境请删除此文件
 *
 * @author system
 * @date 2025/11/03
 */
@RestController
@RequestMapping("/test")
public class TestController extends BaseController {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 生成BCrypt密码哈希
     * 访问: http://localhost:8919/test/generatePassword?password=admin123
     */
    @GetMapping("/generatePassword")
    public Result<Map<String, String>> generatePassword(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        
        Map<String, String> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("verify", String.valueOf(passwordEncoder.matches(password, hash)));
        
        return success(result);
    }
    
    /**
     * 验证密码
     * 访问: http://localhost:8919/test/verifyPassword?password=admin123&hash=xxx
     */
    @GetMapping("/verifyPassword")
    public Result<Map<String, Object>> verifyPassword(@RequestParam String password, 
                                                       @RequestParam String hash) {
        boolean matches = passwordEncoder.matches(password, hash);
        
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", matches);
        
        return success(result);
    }
}

