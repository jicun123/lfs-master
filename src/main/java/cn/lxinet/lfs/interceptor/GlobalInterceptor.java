package cn.lxinet.lfs.interceptor;

import com.alibaba.fastjson2.JSONObject;
import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.SpringContextUtil;
import cn.lxinet.lfs.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class GlobalInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        JwtConfig jwtConfig = SpringContextUtil.getBean(JwtConfig.class);
        String token = request.getHeader("token");
       if (!jwtConfig.parseToken(token)){
           response.setCharacterEncoding("utf-8");
           response.setContentType("application/json");
           PrintWriter out = response.getWriter();
           out.write(JSONObject.toJSONString(new Result(ErrorCode.TOKEN_INVALID.getCode(), ErrorCode.TOKEN_INVALID.getMsg())));
           out.flush();
           out.close();
           return false;
       }
       return true;
    }

}
