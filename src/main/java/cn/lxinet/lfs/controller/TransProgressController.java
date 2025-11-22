package cn.lxinet.lfs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.lxinet.lfs.service.TransProgressService;
import cn.lxinet.lfs.vo.Result;
import cn.lxinet.lfs.vo.TransProgressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 转码进度
 *
 * @author zcx
 * @date 2023/11/22
 */
@RestController
@RequestMapping("/transProgress")
public class TransProgressController extends BaseController{
    @Autowired
    private TransProgressService transProgressService;

    @GetMapping("/list")
    public Result list(){
        Page<TransProgressVo> page = transProgressService.listByPage(getPageNo(), getPageSize());
        return Result.success(page);
    }

}
