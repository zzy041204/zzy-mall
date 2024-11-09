package com.zzy.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.dto.es.SkuESModel;
import com.zzy.mall.search.config.ZzyElasticSearchConfiguration;
import com.zzy.mall.search.constant.ESConstant;
import com.zzy.mall.search.service.ZzySearchService;
import com.zzy.mall.search.vo.SearchParam;
import com.zzy.mall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZzySearchServiceImpl implements ZzySearchService {

    @Autowired
    RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        // 1.准备检索的请求
        SearchRequest request = buildSearchRequest(searchParam);

        try {
            // 2.执行检索的操作
            SearchResponse response = client.search(request, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
            System.out.println("searchResponse:------>" + response.toString());
            // 3.需要把检索的信息封装成SearchResult
            searchResult = buildSearchResult(response, searchParam);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * 构建检索的请求
     * 模糊匹配，关键匹配
     * 过滤(类别，品牌，属性，价格区间，库存)
     * 排序
     * 分页
     * 高亮
     * 聚合分析
     *
     * @param searchParam
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("zzy_product");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 关键字条件
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("subTitle", searchParam.getKeyword()));
        }
        // 类别检索条件
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", searchParam.getCatalog3Id()));
        }
        // 品牌检索条件
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 库存检索条件
        if (searchParam.getHasStock() != null) {
            String hasStock = searchParam.getHasStock() == 1 ? "true" : "false";
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", hasStock));
        }
        // 价格区间检索条件
        if (StringUtils.isNotBlank(searchParam.getSkuPrice())) {
            String[] prices = searchParam.getSkuPrice().split("_");
            RangeQueryBuilder rangeQueryBuilder = new RangeQueryBuilder("skuPrice");
            if (prices.length == 2) {
                // 200_300
                rangeQueryBuilder.gte(prices[0]);
                rangeQueryBuilder.lte(prices[1]);
            } else if (prices.length == 1) {
                if (searchParam.getSkuPrice().endsWith("_")) {
                    // 200_
                    rangeQueryBuilder.gte(prices[0]);
                }
                if (searchParam.getSkuPrice().startsWith("_")) {
                    // 300_
                    rangeQueryBuilder.lte(prices[0]);
                }
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        // 属性检索条件   attrs=20_8英寸:10英寸&attrs=19_64GB:32GB
        if (searchParam.getAttrs() != null && searchParam.getAttrs().size() > 0) {
            for (String attr : searchParam.getAttrs()) {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                // 拼接组合条件
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQuery, ScoreMode.None);
                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);
        // 排序
        if (StringUtils.isNotBlank(searchParam.getSort())) {
            String[] s = searchParam.getSort().split("_");
            String sortParam = s[0];
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            searchSourceBuilder.sort(sortParam, order);
        }
        // 分页处理
        if (searchParam.getPageNum() != null) {
            // 分页处理 pageSize=5
            // pageNum:1 from 0 [0,1,2,3,4]
            // pageNum:2 from 5 [5,6,7,8,9]
            searchSourceBuilder.from((searchParam.getPageNum() - 1) * ESConstant.PRODUCT_PAGE_SIZE);
            searchSourceBuilder.size(ESConstant.PRODUCT_PAGE_SIZE);
        }
        // 高亮处理
        if (StringUtils.isNotBlank(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("subTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }
        // 聚合分析
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brand_agg").field("brandId").size(10)
                .subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName")).size(10)
                .subAggregation(AggregationBuilders.terms("brand_image_agg").field("brandImg").size(10)));

        searchSourceBuilder.aggregation(AggregationBuilders.terms("category_agg").field("categoryId").size(10)
                .subAggregation(AggregationBuilders.terms("category_name_agg").field("categoryName").size(10)));

        searchSourceBuilder.aggregation(AggregationBuilders.nested("attr_agg", "attrs")
                .subAggregation(AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(10)
                        .subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(10))
                        .subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(10))));
        searchRequest.source(searchSourceBuilder);
        System.out.println("searchRequest:------->" + searchRequest.toString());
        return searchRequest;
    }

    /**
     * 根据检索结果 解析封装为SearchResult对象
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = response.getHits();
        // 1.检索的所有商品信息
        SearchHit[] products = hits.getHits();
        List<SkuESModel> esModels = new ArrayList<>();
        if (products != null && products.length > 0) {
            for (SearchHit product : products) {
                String sourceAsString = product.getSourceAsString();
                // 利用fastjson把json格式的字符串转换为SkuESModel对象
                SkuESModel skuESModel = JSON.parseObject(sourceAsString, SkuESModel.class);
                if (StringUtils.isNotBlank(searchParam.getKeyword())) {
                    // 设置高亮
                    HighlightField subTitle = product.getHighlightFields().get("subTitle");
                    String subTitleHighlight = subTitle.getFragments()[0].string();
                    skuESModel.setSubTitle(subTitleHighlight);
                }
                esModels.add(skuESModel);
            }
        }
        searchResult.setProducts(esModels);
        // 2.当前商品涉及到的所有的品牌
        List<SearchResult.BrandVO> brandVOs = new ArrayList<>();
        Aggregations aggregations = response.getAggregations();
        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        if (buckets != null && buckets.size() > 0) {
            for (Terms.Bucket bucket : buckets) {
                SearchResult.BrandVO brandVO = new SearchResult.BrandVO();
                // 设置品牌编号
                Number keyAsNumber = bucket.getKeyAsNumber();
                brandVO.setBrandId(keyAsNumber.longValue());
                Aggregations bucketAggregations = bucket.getAggregations();
                // 设置品牌图片
                ParsedStringTerms brandImageAgg = bucketAggregations.get("brand_image_agg");
                List<? extends Terms.Bucket> brandImageAggBuckets = brandImageAgg.getBuckets();
                if (brandImageAggBuckets != null && brandImageAggBuckets.size() > 0) {
                    brandVO.setBrandImg(brandImageAggBuckets.get(0).getKeyAsString());
                }
                // 设置品牌名称
                ParsedStringTerms brandNameAgg = bucketAggregations.get("brand_name_agg");
                brandVO.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());
                brandVOs.add(brandVO);
            }
        }
        searchResult.setBrands(brandVOs);
        // 3.当前商品涉及到的所有的类别信息
        List<SearchResult.CatalogVO> catalogVOs = new ArrayList<>();
        ParsedLongTerms categoryAgg = aggregations.get("category_agg");
        List<? extends Terms.Bucket> categoryAggBuckets = categoryAgg.getBuckets();
        if (categoryAggBuckets != null && categoryAggBuckets.size() > 0) {
            for (Terms.Bucket bucket : categoryAggBuckets) {
                SearchResult.CatalogVO catalogVO = new SearchResult.CatalogVO();
                // 设置类别id
                Number keyAsNumber = bucket.getKeyAsNumber();
                catalogVO.setCategoryId(keyAsNumber.longValue());
                Aggregations bucketAggregations = bucket.getAggregations();
                // 设置类别名称
                ParsedStringTerms categoryNameAgg = bucketAggregations.get("category_name_agg");
                catalogVO.setCategoryName(categoryNameAgg.getBuckets().get(0).getKeyAsString());
                catalogVOs.add(catalogVO);
            }
        }
        searchResult.setCatalogs(catalogVOs);
        // 4.当前商品设计到的所有的属性信息
        List<SearchResult.AttrVO> attrVOs = new ArrayList<>();
        ParsedNested attrAgg = aggregations.get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (attrIdAggBuckets != null && attrIdAggBuckets.size() > 0) {
            for (Terms.Bucket bucket : attrIdAggBuckets) {
                SearchResult.AttrVO attrVO = new SearchResult.AttrVO();
                // 设置属性id
                attrVO.setAttrId(bucket.getKeyAsNumber().longValue());
                Aggregations bucketAggregations = bucket.getAggregations();
                // 设置属性名称
                ParsedStringTerms attrNameAgg = bucketAggregations.get("attr_name_agg");
                attrVO.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                // 设置属性值
                ParsedStringTerms attrValueAgg = bucketAggregations.get("attr_value_agg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                List<String> attrValues = new ArrayList<>();
                if (attrValueAggBuckets != null && attrValueAggBuckets.size() > 0) {
                    for (Terms.Bucket attrValueAggBucket : attrValueAggBuckets) {
                        attrValues.add(attrValueAggBucket.getKeyAsString());
                    }
                }
                attrVO.setAttrValue(attrValues);
                attrVOs.add(attrVO);
            }
        }
        searchResult.setAttrs(attrVOs);
        // 5.分页信息 当前页 总的记录数 总页数
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total); // 设置总记录数
        searchResult.setPageNum(searchParam.getPageNum()); // 设置当前页数
        int totalPages = total % ESConstant.PRODUCT_PAGE_SIZE == 0 ? (int) total / ESConstant.PRODUCT_PAGE_SIZE : (int) total / ESConstant.PRODUCT_PAGE_SIZE + 1;
        searchResult.setTotalPages(totalPages); //设置总的页数
        List<Integer> navs = new ArrayList<>();
        for (int i = 1 ; i <= totalPages ; i++){
            navs.add(i);
        }
        searchResult.setNavs(navs);
        System.out.println(searchResult);
        return searchResult;
    }


}

