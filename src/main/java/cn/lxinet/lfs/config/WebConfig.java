package cn.lxinet.lfs.config;

import cn.lxinet.lfs.interceptor.GlobalInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> patterns = new ArrayList<>();
        patterns.add("/getToken");
        patterns.add("/user/login");      // 用户登录接口
        patterns.add("/user/register");   // 用户注册接口
        patterns.add("/test/**");         // 测试接口（仅开发环境）
        patterns.add("/api/fs/**");       // 文件预览/下载（通过Nginx代理）
        patterns.add("/fs/**");           // 文件预览/下载（直接访问）
        registry.addInterceptor(new GlobalInterceptor()).addPathPatterns("/**").excludePathPatterns(patterns);
    }

}
