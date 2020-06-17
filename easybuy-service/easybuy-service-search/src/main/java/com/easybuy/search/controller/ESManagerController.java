package com.easybuy.search.controller;

import com.easybuy.entity.Result;
import com.easybuy.entity.StatusCode;
import com.easybuy.search.service.ESManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/manager")
public class ESManagerController {
    private ESManagerService esManagerService;

    //创建索引库结构
    @GetMapping("/create")
    public Result create() {
        esManagerService.createMappingAndIndex();
        return new Result(true, StatusCode.OK, "创建索引库结构成功");
    }

    //导入全部数据
    @GetMapping("/importAll")
    public Result importAll() {
        esManagerService.importAll();
        return new Result(true, StatusCode.OK, "导入全部信息成功");
    }
}
