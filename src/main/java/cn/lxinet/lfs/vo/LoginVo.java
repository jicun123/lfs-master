package cn.lxinet.lfs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回信息
 *
 * @author system
 * @date 2025/11/03
 */
@Data
public class LoginVo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 令牌
     */
    private String token;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 角色，0管理员，1普通用户
     */
    private Integer role;
}

