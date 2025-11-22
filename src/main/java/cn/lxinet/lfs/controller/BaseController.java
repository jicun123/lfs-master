package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.exception.BaseException;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.SpringContextUtil;
import cn.lxinet.lfs.vo.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.Map;

/**
 * 基本控制器
 *
 * @author zcx
 * @date 2023/11/09
 */
public class BaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseController.class);
    @Resource
    protected HttpServletRequest request;
    @Resource
    protected HttpServletResponse response;

    protected Long getPageNo(){
        String pageNoStr = getParam("pageNo");
        Long pageNo;
        try {
            pageNo = Long.parseLong(pageNoStr);
        }catch (Exception e){
            pageNo = 1L;
        }
        return pageNo;
    }

    protected Long getPageSize(){
        String pageSizeStr = getParam("pageSize");
        Long pageSize;
        try {
            pageSize = Long.parseLong(pageSizeStr);
        }catch (Exception e){
            pageSize = 10L;
        }
        return pageSize;
    }

    protected String getParam(String name){
        return request.getParameter(name);
    }

    protected String getParam(String name, String defaultValue){
        String value = getParam(name);
        if (null == value){
            value = defaultValue;
        }
        return value;
    }

    /**
     * 从token中获取用户ID
     * @return 用户ID
     */
    protected Long getUserId() {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        
        JwtConfig jwtConfig = SpringContextUtil.getBean(JwtConfig.class);
        Map<String, String> userInfo = jwtConfig.getUserInfoFromToken(token);
        if (userInfo == null || !userInfo.containsKey("userId")) {
            return null;
        }
        
        try {
            return Long.parseLong(userInfo.get("userId"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从token中获取用户角色
     * @return 角色，0管理员，1普通用户
     */
    protected Integer getUserRole() {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            return null;
        }
        
        JwtConfig jwtConfig = SpringContextUtil.getBean(JwtConfig.class);
        Map<String, String> userInfo = jwtConfig.getUserInfoFromToken(token);
        if (userInfo == null || !userInfo.containsKey("role")) {
            return null;
        }
        
        try {
            return Integer.parseInt(userInfo.get("role"));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 判断当前用户是否为管理员
     * @return true 是管理员
     */
    protected boolean isAdmin() {
        Integer role = getUserRole();
        return role != null && role == 0;
    }

    /**
     * 获取客户端IP地址
     * @return IP地址
     */
    protected String getIpAddress() {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_FORWARDED");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_VIA");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取当前用户名
     * @return 用户名
     */
    protected String getUsername() {
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            return "";
        }
        
        JwtConfig jwtConfig = SpringContextUtil.getBean(JwtConfig.class);
        Map<String, String> userInfo = jwtConfig.getUserInfoFromToken(token);
        if (userInfo == null || !userInfo.containsKey("username")) {
            return "";
        }
        
        return userInfo.get("username");
    }

    protected String getErrorMessages(BindingResult result) {
        List<FieldError> list = result.getFieldErrors();
        if (list.size() > 0){
            return list.get(0).getDefaultMessage();
        }
        return "";
    }

    /**
     * 判断是否是AJAX请求
     * @return true ajax
     */
    protected boolean isAjaxRequest(){
        String header = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equalsIgnoreCase(header) ? true : false;
    }

    /**
     * 全局捕获异常
     * @param e 捕获到的异常
     */
    @ExceptionHandler
    public Result execptionHandler(Exception e){
        Result<?> result = new Result();
        result.setCode(ErrorCode.SYSTEM_EXCEPTION.getCode());
        if (null == e.getMessage()){
            result.setMsg("系统异常：" + e.toString());
        }else {
            result.setMsg(e.getMessage());
        }
        LOGGER.error("系统异常", e);
        return result;
    }


    /**
     * 全局捕获异常
     * @param e 捕获到的异常
     */
    @ExceptionHandler
    public Result execptionHandler(BaseException e){
        Result<?> result = new Result();
        if (e.getCode() == ErrorCode.PARAM_ERROR.getCode()){
            result.setMsg(null != e.getMsg() ? e.getMsg() : ErrorCode.PARAM_ERROR.getMsg());
            result.setCode(ErrorCode.PARAM_ERROR.getCode());
        }else {
            result.setMsg(null != e.getMsg() ? e.getMsg() : ErrorCode.SYSTEM_EXCEPTION.getMsg());
            result.setCode(null != e.getCode() ? e.getCode() : ErrorCode.SYSTEM_EXCEPTION.getCode());
        }
        return result;
    }

    /**
     * 全局捕获异常
     * @param e 捕获到的异常
     */
    @ExceptionHandler
    public Result execptionHandler(BindException e){
        Result<?> result = new Result();
        result.setMsg(getErrorMessages(e.getBindingResult()));
        result.setCode(ErrorCode.PARAM_ERROR.getCode());
        return result;
    }

    /**
     * 全局捕获异常
     * @param e 捕获到的异常
     */
    @ExceptionHandler
    public Result execptionHandler(MethodArgumentNotValidException e){
        Result<?> result = new Result();
        result.setMsg(getErrorMessages(e.getBindingResult()));
        result.setCode(ErrorCode.PARAM_ERROR.getCode());
        return result;
    }

    /**
     * 返回成功结果
     * @param data 数据
     */
    protected <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMsg(ErrorCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 返回错误结果
     * @param msg 错误信息
     */
    protected <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SYSTEM_EXCEPTION.getCode());
        result.setMsg(msg);
        return result;
    }
}
