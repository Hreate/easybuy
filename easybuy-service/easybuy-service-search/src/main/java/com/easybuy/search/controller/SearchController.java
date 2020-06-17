package com.easybuy.search.controller;

import com.easybuy.entity.Page;
import com.easybuy.search.pojo.SkuInfo;
import com.easybuy.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Controller
@RequestMapping("/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping()
    @ResponseBody
    public Map search(@RequestParam Map<String, String> searchMap) {
        //特殊符号处理
        this.handleSearchMap(searchMap);
        Map searchResult = searchService.search(searchMap);
        return searchResult;
    }

    private void handleSearchMap(Map<String, String> searchMap) {
        Set<Map.Entry<String, String>> entries = searchMap.entrySet();
        entries.stream()
                .filter(entry -> entry.getKey().startsWith("spec_"))
                .map(entry -> searchMap.put(entry.getKey(), entry.getValue().replace("+", "%2B")));
    }

    @GetMapping("/list")
    public String list(@RequestParam Map<String, String> searchMap, Model model) {
        Map resultMap = null;
        if (searchMap != null && searchMap.size() > 0) {
            //处理特殊符号
            this.handleSearchMap(searchMap);
            //获取查询结果
            resultMap = searchService.search(searchMap);
            model.addAttribute("result", resultMap);
            model.addAttribute("searchMap", searchMap);
        }
        //封装分页数据并返回
        /*
        第一个参数：总记录数
        第二个参数：当前页
        第三个参数：每页显示的条数
         */
        Page<SkuInfo> page = new Page<>(
                Long.parseLong(String.valueOf(resultMap.get("total"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageNum"))),
                Page.pageSize
        );
        model.addAttribute("page", page);
        //拼装url
        StringBuilder builder = new StringBuilder("/search/list");
        if (searchMap != null && searchMap.size() > 0) {
            //有查询条件
            builder.append("?");
            for (String key : searchMap.keySet()) {
                if (!"sortRule".equals(key) && !"sortField".equals(key) && !"pageNum".equals(key)) {
                    builder.append(key).append("=").append(searchMap.get(key)).append("&");
                }
            }
            String urlTemp = builder.toString();
            String url = urlTemp.substring(0, urlTemp.length() - 1);
            model.addAttribute("url", url);
        } else {
            model.addAttribute("url", builder.toString());
        }
        return "search";
    }
}
