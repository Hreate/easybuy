package com.easybuy.search.service.impl;

import com.easybuy.search.pojo.SkuInfo;
import com.easybuy.search.service.SearchService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SearchServiceImpl implements SearchService {
    private final ElasticsearchRestTemplate template;
    private final ObjectMapper objectMapper;

    @Override
    public Map search(Map<String, String> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //构建查询
        if (searchMap != null) {
            //构建查询条件封装对象
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            //按照关键字查询
            if (StringUtils.isNotEmpty(searchMap.get("keywords"))) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("name", searchMap.get("keywords")).operator(Operator.AND));
            }
            //按照品牌进行过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }
            //按照规格进行过滤查询
            searchMap.keySet().stream()
                    .filter(key -> key.startsWith("spec_"))
                    .forEach(key -> {
                        String value = searchMap.get(key).replace("%2B", "+");
                        boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                    });
            //按照价格进行区间过滤查询
            if (StringUtils.isNotEmpty(searchMap.get("price"))) {
                String[] prices = searchMap.get("price").split("-");
                if (prices.length == 2) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]).lte(prices[1]));
                }
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(prices[0]));
            }
            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
            //按照品牌进行聚合查询
            String skuBrand = "skuBrand";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuBrand).field("brandName"));
            //按照规格进行聚合查询
            String skuSpec = "skuSpec";
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(skuSpec).field("spec.keyword"));
            //开启分页查询
            String pageNum = searchMap.get("pageNum"); //当前页
            String pageSize = searchMap.get("pageSize"); //每页的条数
            if (StringUtils.isEmpty(pageNum)) {
                pageNum = "1";
            }
            if (StringUtils.isEmpty(pageSize)) {
                pageSize = "30";
            }
            //设置分页
            nativeSearchQueryBuilder.withPageable(PageRequest.of(Integer.parseInt(pageNum) - 1, Integer.parseInt(pageSize)));
            //按照相关字段进行排序查询
            //1.当前字段 2.当前的排序操作（升序ASC，降序DESC）
            if (StringUtils.isNotEmpty(searchMap.get("sortField")) && StringUtils.isNotEmpty(searchMap.get("sortRule"))) {
                if ("ASC".equals(searchMap.get("sortRule"))) {
                    //升序
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.ASC));
                } else {
                    nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(searchMap.get("sortField")).order(SortOrder.DESC));
                }
            }
            //设置高亮字段及高亮样式
            HighlightBuilder.Field field = new HighlightBuilder.Field("name") //高亮字段
                    .preTags("<span style='color:red'>")
                    .postTags("</span>");
            nativeSearchQueryBuilder.withHighlightFields(field);
            /*
            开启查询
            第一个参数：条件构建对象
            第二个参数：查询操作实体类
            第三个参数：查询结果操作对象
             */
            AggregatedPage<SkuInfo> resultInfo = template.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    //查询结果操作
                    List<T> list = new ArrayList<>();
                    //获取查询命中结果
                    SearchHits hits = searchResponse.getHits();
                    if (hits != null) {
                        //有查询结果
                        //SearchHit转换为skuinfo
                        for (var hit : hits) {
                            try {
                                SkuInfo skuInfo = objectMapper.readValue(hit.getSourceAsString(), SkuInfo.class);
                                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                                if (highlightFields != null && highlightFields.size() > 0) {
                                    //替换为高亮数据
                                    skuInfo.setName(highlightFields.get("name").getFragments()[0].toString());
                                }
                                list.add((T) skuInfo);
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return new AggregatedPageImpl<T>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
            //封装查询结果
            //总记录数
            resultMap.put("total", resultInfo.getTotalElements());
            //总页数
            resultMap.put("totalPages", resultInfo.getTotalPages());
            //数据集合
            resultMap.put("rows", resultInfo.getContent());
            //封装品牌的聚合结果
            ParsedStringTerms brandTerms = (ParsedStringTerms) resultInfo.getAggregation(skuBrand);
            List<String> brandList = brandTerms.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            resultMap.put("brandList", brandList);
            //封装规格的聚合结果
            ParsedStringTerms specTerms = (ParsedStringTerms) resultInfo.getAggregation(skuSpec);
            List<String> specList = specTerms.getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                    .collect(Collectors.toList());
            resultMap.put("specList", this.formatSpec(specList));
            //封装当前页
            resultMap.put("pageNum", pageNum);
        }
        return resultMap;
    }

    public Map<String, Set<String>> formatSpec(List<String> speList) {
        Map<String, Set<String>> resultMap = new HashMap<>();
        if(speList != null && speList.size() > 0) {
            for (String specJson : speList) {
                //将json转换为map
                try {
                    Map<String, String> specMap = objectMapper.readValue(specJson, Map.class);
                    for (String specKey : specMap.keySet()) {
                        Set<String> specSet = resultMap.get(specKey);
                        if (specSet == null) {
                            specSet = new HashSet<>();
                        }
                        //将规格的值放入set
                        specSet.add(specMap.get(specKey));
                        //将set放入resultMap中
                        resultMap.put(specKey, specSet);
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
        return resultMap;
    }
}
