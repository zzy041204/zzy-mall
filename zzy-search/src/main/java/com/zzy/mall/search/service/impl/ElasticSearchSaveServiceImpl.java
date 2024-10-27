package com.zzy.mall.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzy.mall.common.dto.es.SkuESModel;
import com.zzy.mall.search.config.ZzyElasticSearchConfiguration;
import com.zzy.mall.search.constant.ESConstant;
import com.zzy.mall.search.service.ElasticSearchSaveService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ElasticSearchSaveServiceImpl implements ElasticSearchSaveService {

    @Autowired
    private RestHighLevelClient client;

    /**
     * 实现上架数据存储到ES的操作
     * @param skuESModels
     * @return
     */
    @Override
    public Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException {
        // 创建对应的索引库 创建对应product的映射
        // BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuESModel skuESModel : skuESModels) {
            // 绑定对应的索引库 product
            IndexRequest indexRequest = new IndexRequest(ESConstant.PRODUCT_INDEX);
            // 设置id
            indexRequest.id(skuESModel.getSkuId().toString());
            // 设置文档
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(skuESModel);
            indexRequest.source(json, XContentType.JSON);
            // 转换后的数据封装到Bulk中
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, ZzyElasticSearchConfiguration.COMMON_OPTIONS);
        boolean failures = bulk.hasFailures();
        if (failures) {
            return false;
        }else {
            return true;
        }
    }
}
