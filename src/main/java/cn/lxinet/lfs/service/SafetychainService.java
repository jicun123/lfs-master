package cn.lxinet.lfs.service;

import com.alibaba.fastjson2.JSON;
import cn.lxinet.lfs.utils.Base64Util;
import cn.lxinet.lfs.vo.SafetychainVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * SafetyChain 服务
 * 文件防盗链处理
 * @author zcx
 * @date 2023/11/22
 */
@Service
public class SafetychainService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SafetychainService.class);
    //过期时长，单位秒
    @Value("${config.file-server.local.st-effective-time}")
    public Integer stEffectiveTime;
    @Value("${config.file-server.local.secret}")
    private String secret;

    /**
     * 获取加上加密串的地址
     * 比如传：/files/5e1757f6-749b-480e-8bfa-f05f2ab906c7.pptx
     * 返回结果：/files/5e1757f6-749b-480e-8bfa-f05f2ab906c7.pptx?secret=MHxN8fxa7YQy6cTsfMaPNA&expire=1705892879
     * @param path
     * @return
     */
    public String getEncryUrl(String path){
        SafetychainVo sc = encrySc(path);
        if (path.indexOf("?") > -1){
            return path + "&secret=" + sc.getSecret() + "&expire=" + sc.getExpiredTime();
        }else {
            return path + "?secret=" + sc.getSecret() + "&expire=" + sc.getExpiredTime();
        }
    }

    /**
     * 加密
     * 如果需要SafetychainVo对象，就调用这个方法
     * 可以从对象获取st、expiredTime、path
     * @param path
     * @return
     */
    public SafetychainVo encrySc(String path){
        SafetychainVo sc;
        try {
            if (StringUtils.isBlank(path)){
                return new SafetychainVo();
            }
            Long expiredTime = System.currentTimeMillis() / 1000 + stEffectiveTime;
            String st = Base64Util.encode(DigestUtils.md5(secret + path + expiredTime)).replace("=", "");
            sc = new SafetychainVo(st, expiredTime, path);
        }catch (Exception e){
            sc = new SafetychainVo();
            LOGGER.error("防盗链加密出现异常", e);
        }
        return sc;
    }

    /**
     * 校验防盗链参数
     * @param path    文件相对路径
     * @param st      加密串
     * @param expire  过期时间（秒）
     * @return 是否有效
     */
    public boolean validate(String path, String st, Long expire){
        if (StringUtils.isBlank(path) || StringUtils.isBlank(st) || expire == null){
            return false;
        }
        long current = System.currentTimeMillis() / 1000;
        if (expire < current){
            LOGGER.warn("防盗链参数已过期，path：{}，expire：{}，current：{}", path, expire, current);
            return false;
        }
        String expected = Base64Util.encode(DigestUtils.md5(secret + path + expire)).replace("=", "");
        boolean match = expected.equals(st);
        if (!match){
            LOGGER.warn("防盗链校验失败，path：{}，expire：{}，expected：{}，actual：{}", path, expire, expected, st);
        }
        return match;
    }


    public static void main(String[] args) {
        String secret = "e9eaa184ac1b4068829edb4f3ea978f4";
        Integer stEffectiveTime = 3000;
        Long expiredTime = System.currentTimeMillis() / 1000 + stEffectiveTime;
        String path = "/files/774160ba-79f1-468c-8198-6baf85992dcf.mp3";
        String st = Base64Util.encode(DigestUtils.md5(secret + path + expiredTime)).replace("=", "");
        SafetychainVo sc = new SafetychainVo(st, expiredTime, path);
        System.out.println(JSON.toJSONString(sc));
        System.out.println(sc.getUrl() + "?secret=" + sc.getSecret() + "&expire=" + sc.getExpiredTime());
        //        System.out.println(new SafetychainService().getEncryUrl(1L,  "/files/85/DDXK/2021/05/31/DDXKXS_888802_1622447880521.mp4"));
//        System.out.println(new SafetychainService().getEncryName(1L,  "/files/85/DDXK/2021/05/31/", "DDXKXS_888802_1622447880521.mp4"));
//        System.out.println(new SafetychainService().getEncryName(1L,  "/files/85/DDXK/2021/05/31/", "DDXKXS_888802_1622447880521.mp4", "&f=list"));
    }
}
