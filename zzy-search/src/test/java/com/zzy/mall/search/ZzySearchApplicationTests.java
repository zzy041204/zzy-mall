package com.zzy.mall.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzy.mall.search.config.ZzyElasticSearchConfiguration;
import lombok.Data;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ZzySearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    /**
     * 实现商品数据的检索,我们需要把相关的信息存储到ES中
     * {
     *     skuId:11,
     *     spuId:9,
     *     skuTitle:华为,
     *     price:5999,
     *     saleCount:100,
     *     attrs:[
     *     {机身内存:256G},
     *     {屏幕尺寸:85英寸},
     *     {CPU型号:骁龙xxx}
     *     ]
     * }
     */

    @Test
    void contextLoads() {
        System.out.println(client);
    }

    @Test
    void searchIndexResponse() throws IOException {
        // 1.创建一个SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("blank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询出blank下 address中包含mill的记录
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchRequest.source(searchSourceBuilder);
        // 2.执行检索操作
        SearchResponse response = client.search(searchRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象 检索出我们关心的数据
        RestStatus status = response.status();
        TimeValue took = response.getTook();
        SearchHits hits = response.getHits();
        TotalHits totalHits = hits.getTotalHits();
        TotalHits.Relation relation = totalHits.relation;
        long value = totalHits.value;
        float maxScore = hits.getMaxScore(); // 相关性最高分
        SearchHit[] hits1 = hits.getHits();

        System.out.println(relation + "----->" + value + "----->" + status);
    }

    /**
     * 聚合统计
     * @throws IOException
     */
    @Test
    void searchIndexAggregation() throws IOException {
        // 1.创建一个SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("blank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询出blank下所有的文档
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(0);
        // 聚合 aggregation
        /*// 聚合blank下 年龄的分布和每个年龄的平均薪资及平均年龄
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age")
                        .subAggregation(AggregationBuilders.avg("BalanceAvg").field("balance")))
                .aggregation(AggregationBuilders.avg("ageAvg").field("age"));*/
        // 查出所有年龄分布，并且这些年龄段中M的平均薪资和F的平均薪资以及这个年龄段的总体平均薪资
        searchSourceBuilder.aggregation(AggregationBuilders.terms("age").field("age")
                .subAggregation(AggregationBuilders.terms("genderAgg").field("gender.keyword")
                        .subAggregation(AggregationBuilders.avg("genderBalanceAvg").field("balance")))
                .subAggregation(AggregationBuilders.avg("balanceAvg").field("balance")));
        searchRequest.source(searchSourceBuilder);
        // 2.执行检索操作
        SearchResponse search = client.search(searchRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象 检索出我们关心的数据
        System.out.println("ElasticSearch检索的信息：" + search);
    }

    /**
     * 带条件检索
     */
    @Test
    void searchIndexByAddress() throws IOException {
        // 1.创建一个SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("blank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 查询出blank下 address中包含mill的记录
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchRequest.source(searchSourceBuilder);
        // 2.执行检索操作
        SearchResponse search = client.search(searchRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象 检索出我们关心的数据
        System.out.print("ElasticSearch检索的信息：" + search);
    }

    /**
     * 检索所有的数据
     */
    @Test
    void searchIndexAll() throws IOException {
        // 1.创建一个SearchRequest对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("blank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 检索所有数据
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchRequest.source(searchSourceBuilder);
        // 2.执行检索操作
        SearchResponse search = client.search(searchRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象 检索出我们关心的数据
        System.out.print("ElasticSearch检索的信息：" + search);
    }


    /**
     * 测试保存文档
     */
    @Test
    void saveIndex() throws IOException {
        IndexRequest indexRequest = new IndexRequest("posts");
        indexRequest.id("1");
//        indexRequest.source("name", "zzy",
//                "age", 18,
//                "gender", "男");
        User user = new User();
        user.setName("zzy");
        user.setAge(19);
        user.setGender("男");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        indexRequest.source(json, XContentType.JSON);
        // 执行操作
        IndexResponse index = client.index(indexRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        // 提取有用的返回信息
        System.out.println(index);
    }

    @Data
    class User{
        private String name;
        private Integer age;
        private String gender;
    }


}
