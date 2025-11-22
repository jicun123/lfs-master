package cn.lxinet.lfs.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SafetychainVo implements Serializable {
    //加密串
    private String secret;
    //截止时间
    private Long expiredTime;
    //资源地址
    private String url;

    public SafetychainVo(){

    }

    public SafetychainVo(String secret, Long expiredTime, String url){
        this.secret = secret;
        this.expiredTime = expiredTime;
        this.url = url;
    }

}
