package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.vo.LoginVo;
import cn.lxinet.lfs.vo.UserVo;

/**
 * 用户服务
 *
 * @author system
 * @date 2025/11/03
 */
public interface UserService {
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录返回信息
     */
    LoginVo login(String username, String password);
    
    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @return 用户信息
     */
    User register(String username, String password, String nickname);
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVo getUserInfo(Long userId);
    
    // ==================== 管理员专用方法 ====================
    
    /**
     * 获取用户列表（分页）
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    Object getUserList(Long pageNo, Long pageSize, String keyword);
    
    /**
     * 添加用户
     * 
     * @param username 用户名
     * @param password 密码
     * @param nickname 昵称
     * @param role 角色
     * @return 用户信息
     */
    User addUser(String username, String password, String nickname, Integer role);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param password 密码
     * @param role 角色
     * @param status 状态
     */
    void updateUser(Long userId, String nickname, String password, Integer role, Integer status);
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     */
    void deleteUser(Long userId);
    
    /**
     * 切换用户状态
     * 
     * @param userId 用户ID
     */
    void toggleStatus(Long userId);
}

