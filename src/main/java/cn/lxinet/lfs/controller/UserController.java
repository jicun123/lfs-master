package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.service.UserService;
import cn.lxinet.lfs.vo.LoginVo;
import cn.lxinet.lfs.vo.Result;
import cn.lxinet.lfs.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author system
 * @date 2025/11/03
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVo> login(@RequestParam String username, @RequestParam String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        
        LoginVo loginVo = userService.login(username, password);
        return success(loginVo);
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<User> register(@RequestParam String username, 
                                   @RequestParam String password,
                                   @RequestParam(required = false, defaultValue = "") String nickname) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            return error("用户名长度必须在3-20个字符之间");
        }
        
        if (password.length() < 6) {
            return error("密码长度不能少于6个字符");
        }
        
        if (StringUtils.isBlank(nickname)) {
            nickname = username;
        }
        
        User user = userService.register(username, password, nickname);
        return success(user);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserVo> getUserInfo() {
        Long userId = getUserId();
        if (userId == null) {
            return error("未登录或登录已过期");
        }
        
        UserVo userVo = userService.getUserInfo(userId);
        return success(userVo);
    }
    
    // ==================== 管理员专用接口 ====================
    
    /**
     * 获取用户列表（仅管理员）
     */
    @GetMapping("/list")
    public Result<?> getUserList(@RequestParam(defaultValue = "1") Long pageNo,
                                   @RequestParam(defaultValue = "10") Long pageSize,
                                   @RequestParam(required = false) String keyword) {
        if (!isAdmin()) {
            return error("无权限访问，仅管理员可用");
        }
        
        return success(userService.getUserList(pageNo, pageSize, keyword));
    }
    
    /**
     * 添加用户（仅管理员）
     */
    @PostMapping("/add")
    public Result<User> addUser(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam(required = false, defaultValue = "") String nickname,
                                  @RequestParam(required = false, defaultValue = "1") Integer role) {
        if (!isAdmin()) {
            return error("无权限操作，仅管理员可用");
        }
        
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return error("用户名或密码不能为空");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            return error("用户名长度必须在3-20个字符之间");
        }
        
        if (password.length() < 6) {
            return error("密码长度不能少于6个字符");
        }
        
        if (StringUtils.isBlank(nickname)) {
            nickname = username;
        }
        
        User user = userService.addUser(username, password, nickname, role);
        return success(user);
    }
    
    /**
     * 更新用户（仅管理员）
     */
    @PostMapping("/update")
    public Result<?> updateUser(@RequestParam Long userId,
                                 @RequestParam(required = false) String nickname,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(required = false) Integer role,
                                 @RequestParam(required = false) Integer status) {
        if (!isAdmin()) {
            return error("无权限操作，仅管理员可用");
        }
        
        if (userId == null) {
            return error("用户ID不能为空");
        }
        
        userService.updateUser(userId, nickname, password, role, status);
        return success(null);
    }
    
    /**
     * 删除用户（仅管理员）
     */
    @PostMapping("/delete")
    public Result<?> deleteUser(@RequestParam Long userId) {
        if (!isAdmin()) {
            return error("无权限操作，仅管理员可用");
        }
        
        if (userId == null) {
            return error("用户ID不能为空");
        }
        
        // 不能删除自己
        if (userId.equals(getUserId())) {
            return error("不能删除当前登录的管理员账号");
        }
        
        userService.deleteUser(userId);
        return success(null);
    }
    
    /**
     * 切换用户状态（仅管理员）
     */
    @PostMapping("/toggleStatus")
    public Result<?> toggleStatus(@RequestParam Long userId) {
        if (!isAdmin()) {
            return error("无权限操作，仅管理员可用");
        }
        
        if (userId == null) {
            return error("用户ID不能为空");
        }
        
        // 不能禁用自己
        if (userId.equals(getUserId())) {
            return error("不能禁用当前登录的管理员账号");
        }
        
        userService.toggleStatus(userId);
        return success(null);
    }
}

