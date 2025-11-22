package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.dto.TransTemplateDto;
import cn.lxinet.lfs.entity.TransTemplate;
import cn.lxinet.lfs.service.TransTemplateService;
import cn.lxinet.lfs.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 转码模板
 *
 * @author zcx
 * @date 2023/11/27
 */
@RestController
@RequestMapping("/transTemplate")
public class TransTemplateController extends BaseController{
    @Autowired
    private TransTemplateService transTemplateService;

    @GetMapping("/list")
    public Result list(){
        List<TransTemplate> list = transTemplateService.list();
        return Result.success(list);
    }

    @PostMapping("/save")
    public Result save(@Valid TransTemplateDto dto){
        if (dto.getId() == null){
            transTemplateService.save(dto);
        }else {
            transTemplateService.update(dto);
        }
        return Result.success();
    }

    @PostMapping("/delete")
    public Result save(Long id){
        transTemplateService.delete(id);
        return Result.success();
    }

    @PostMapping("/updateStatus")
    public Result updateStatus(Long id, Integer status){
        transTemplateService.updateStatus(id, status);
        return Result.success();
    }

}
