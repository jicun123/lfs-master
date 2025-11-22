package cn.lxinet.lfs.service.impl;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.entity.User;
import cn.lxinet.lfs.exception.BaseException;
import cn.lxinet.lfs.mapper.UserMapper;
import cn.lxinet.lfs.service.UserService;
import cn.lxinet.lfs.vo.LoginVo;
import cn.lxinet.lfs.vo.UserVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 *
 * @author system
 * @date 2025/11/03
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtConfig jwtConfig;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Override
    public LoginVo login(String username, String password) {
        // 查询用户
        User user = getUserByUsername(username);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BaseException("用户已被禁用");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BaseException("密码错误");
        }
        
        // 生成token
        String token = jwtConfig.genUserToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构造返回信息
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserId(user.getId());
        loginVo.setUsername(user.getUsername());
        loginVo.setNickname(user.getNickname());
        loginVo.setRole(user.getRole());
        
        return loginVo;
    }
    
    @Override
    public User register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        User existUser = getUserByUsername(username);
        if (existUser != null) {
            throw new BaseException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(1); // 默认为普通用户
        user.setStatus(1); // 默认启用
        
        userMapper.insert(user);
        
        // 清除密码字段
        user.setPassword(null);
        
        return user;
    }
    
    @Override
    public User getUserByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
    
    @Override
    public UserVo getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        UserVo userVo = new UserVo();
        userVo.setUserId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setNickname(user.getNickname());
        userVo.setRole(user.getRole());
        userVo.setStatus(user.getStatus());
        
        return userVo;
    }
    
    // ==================== 管理员专用方法实现 ====================
    
    @Override
    public Object getUserList(Long pageNo, Long pageSize, String keyword) {
        Page<User> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                   .or()
                   .like(User::getNickname, keyword);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = userMapper.selectPage(page, wrapper);
        
        // 转换为VO，隐藏密码
        List<UserVo> userVoList = userPage.getRecords().stream().map(user -> {
            UserVo vo = new UserVo();
            vo.setUserId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setRole(user.getRole());
            vo.setStatus(user.getStatus());
            return vo;
        }).collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", userVoList);
        result.put("total", userPage.getTotal());
        result.put("current", userPage.getCurrent());
        result.put("size", userPage.getSize());
        
        return result;
    }
    
    @Override
    public User addUser(String username, String password, String nickname, Integer role) {
        // 检查用户名是否已存在
        User existUser = getUserByUsername(username);
        if (existUser != null) {
            throw new BaseException("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole(role);
        user.setStatus(1); // 默认启用
        
        userMapper.insert(user);
        
        // 清除密码字段
        user.setPassword(null);
        
        return user;
    }
    
    @Override
    public void updateUser(Long userId, String nickname, String password, Integer role, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId);
        
        if (StringUtils.isNotBlank(nickname)) {
            wrapper.set(User::getNickname, nickname);
        }
        
        if (StringUtils.isNotBlank(password)) {
            wrapper.set(User::getPassword, passwordEncoder.encode(password));
        }
        
        if (role != null) {
            wrapper.set(User::getRole, role);
        }
        
        if (status != null) {
            wrapper.set(User::getStatus, status);
        }
        
        userMapper.update(null, wrapper);
    }
    
    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        userMapper.deleteById(userId);
    }
    
    @Override
    public void toggleStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        
        // 切换状态：0变1，1变0
        Integer newStatus = user.getStatus() == 1 ? 0 : 1;
        
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
               .set(User::getStatus, newStatus);
        
        userMapper.update(null, wrapper);
    }
}

