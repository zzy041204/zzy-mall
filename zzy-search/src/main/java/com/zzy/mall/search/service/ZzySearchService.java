package com.zzy.mall.search.service;

import com.zzy.mall.search.vo.SearchParam;
import com.zzy.mall.search.vo.SearchResult;

public interface ZzySearchService {

    SearchResult search(SearchParam searchParam);

}
