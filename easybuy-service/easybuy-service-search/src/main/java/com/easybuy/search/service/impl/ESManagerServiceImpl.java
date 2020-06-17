package com.easybuy.search.service.impl;

import com.easybuy.goods.feign.SkuFeign;
import com.easybuy.goods.pojo.Sku;
import com.easybuy.search.dao.ESManagerMapper;
import com.easybuy.search.pojo.SkuInfo;
import com.easybuy.search.service.ESManagerService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//import org.springframework.data.elasticsearch.core.IndexOperations;
//import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@Service
public class ESManagerServiceImpl implements ESManagerService {
    @Autowired
    private ElasticsearchRestTemplate template;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ESManagerMapper esManagerMapper;

    //创建索引库结构
    @Override
    public void createMappingAndIndex() {
        //创建索引
//        IndexOperations indexOperations = template.indexOps(SkuInfo.class);
//        Document mapping = indexOperations.createMapping();
//        indexOperations.create(mapping);
        template.createIndex(SkuInfo.class);
        template.putMapping(SkuInfo.class);
    }

    //导入全部sku集合进入到索引库
    @Override
    public void importAll() {
        importDataBySpuId("all");
    }

    //根据spuId查询skuList，添加到索引库
    @Override
    public void importDataBySpuId(String spuId) {
        //查询sku集合
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        Assert.notEmpty(skuList, "当前没有查询到数据，无法导入索引库");
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //将skuList转换为json
        try {
            String skuListJson = objectMapper.writeValueAsString(skuList);
            //将json转换为skuInfo
            List<SkuInfo> skuInfoList = objectMapper.readValue(skuListJson, objectMapper.getTypeFactory().constructCollectionType(List.class, SkuInfo.class));
            skuInfoList.parallelStream().forEach(skuInfo -> {
                try {
                    skuInfo.setSpecMap(objectMapper.readValue(skuInfo.getSpec(), Map.class));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            //导入索引库
            esManagerMapper.saveAll(skuInfoList);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
