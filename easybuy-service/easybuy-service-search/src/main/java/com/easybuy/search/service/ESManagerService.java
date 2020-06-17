package com.easybuy.search.service;

public interface ESManagerService {
    //创建索引库的结构
    void createMappingAndIndex();

    //导入全部数据进es
    void importAll();

    //根据spuId查询skuList，再导入索引库
    void importDataBySpuId(String spuId);
}
