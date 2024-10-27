package com.zzy.mall.search.service;

import com.zzy.mall.common.dto.es.SkuESModel;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchSaveService {
    Boolean productStatusUp(List<SkuESModel> skuESModels) throws IOException;
}
