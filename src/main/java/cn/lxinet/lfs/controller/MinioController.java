package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.utils.MinioUtil;
import cn.lxinet.lfs.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/minio")
public class MinioController {
    @Autowired
    private MinioUtil minioUtil;

    @PostMapping("/getPolicy")
    public Result getPolicy(String fileName){
        Map map = minioUtil.getPolicy(fileName);
        String policyUrl = minioUtil.getPolicyUrl(fileName);
        map.put("policyUrl", policyUrl);
        return Result.success(map);
    }
}
