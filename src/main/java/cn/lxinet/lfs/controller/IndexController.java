package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.config.JwtConfig;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器
 *
 * @author zcx
 * @date 2023/11/25
 */
@RestController
@RequestMapping("/")
public class IndexController extends BaseController{
    @Autowired
    private JwtConfig jwtConfig;

    @PostMapping("/getToken")
    public Result getToken(String appId, String appSecret){
        Assert.isTrue(StringUtils.isNotBlank(appId), ErrorCode.PARAM_ERROR, "appId不能为空");
        Assert.isTrue(StringUtils.isNotBlank(appSecret), ErrorCode.PARAM_ERROR, "appSecret不能为空");
        String token = jwtConfig.genToken(appId, appSecret);
        return Result.success(token);
    }


}
