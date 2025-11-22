package cn.lxinet.lfs.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import cn.lxinet.lfs.exception.BaseException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * jwt配置
 *
 * @author zcx
 * @date 2023/07/09
 */
@Component
public class JwtConfig {
    @Value("${config.app-id}")
    private String appId;
    @Value("${config.app-secret}")
    private String appSecret;
    @Value("${config.token-expire}")
    private long expire;
    private static String ISSUER = "";

    /**
     * 生成token (应用token)
     * @return
     */
    public String genToken(String id, String secret){
        if (!appId.equals(id) || !appSecret.equals(secret)){
            throw new BaseException("appId或者appSecret不正确");
        }
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        JWTCreator.Builder builder = JWT.create().withIssuer(ISSUER).withIssuedAt(new Date()).
                withExpiresAt(new Date((Instant.now().getEpochSecond() + expire) * 1000));
        Map<String, String> claims = new HashMap<>();
        claims.put("appId", appId);
        claims.forEach((key,value)-> builder.withClaim(key, value));
        return builder.sign(algorithm);
    }

    /**
     * 生成用户token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 角色
     * @return token
     */
    public String genUserToken(Long userId, String username, Integer role){
        Algorithm algorithm = Algorithm.HMAC256(appSecret);
        JWTCreator.Builder builder = JWT.create().withIssuer(ISSUER).withIssuedAt(new Date()).
                withExpiresAt(new Date((Instant.now().getEpochSecond() + expire) * 1000));
        Map<String, String> claims = new HashMap<>();
        claims.put("userId", String.valueOf(userId));
        claims.put("username", username);
        claims.put("role", String.valueOf(role));
        claims.put("type", "user"); // 标识为用户token
        claims.forEach((key,value)-> builder.withClaim(key, value));
        return builder.sign(algorithm);
    }

    /**
     * 解析jwt
     * @param token
     * @return
     */
    public boolean parseToken(String token) {
        try {
            if (StringUtils.isBlank(token)){
                return false;
            }
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt =  verifier.verify(token);
            Map<String, Claim> map = jwt.getClaims();
            Map<String, String> resultMap = new HashMap<>();
            map.forEach((k,v) -> resultMap.put(k, v.asString()));
            
            // 判断是用户token还是应用token
            String tokenType = resultMap.get("type");
            if ("user".equals(tokenType)) {
                // 用户token，验证userId是否存在
                String userId = resultMap.get("userId");
                return StringUtils.isNotBlank(userId);
            } else {
                // 应用token，验证appId
                String thisAppId = resultMap.get("appId");
                return appId.equals(thisAppId);
            }
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 从token中获取用户信息
     * @param token
     * @return
     */
    public Map<String, String> getUserInfoFromToken(String token) {
        try {
            if (StringUtils.isBlank(token)){
                return null;
            }
            Algorithm algorithm = Algorithm.HMAC256(appSecret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
            DecodedJWT jwt =  verifier.verify(token);
            Map<String, Claim> map = jwt.getClaims();
            Map<String, String> resultMap = new HashMap<>();
            map.forEach((k,v) -> resultMap.put(k, v.asString()));
            return resultMap;
        }catch (Exception e){
            return null;
        }
    }

}
