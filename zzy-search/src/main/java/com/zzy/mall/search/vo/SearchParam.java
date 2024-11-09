package com.zzy.mall.search.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 封装页面所有可能提交的查询条件
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchParam {

    private String keyword; // 查询全文匹配的关键字
    private Long catalog3Id; // 需要根据分类查询的编号

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort; // 排序条件

    // 查询的筛选条件 hasStock=0/1;
    private Integer hasStock = 1; //是否只显示有货的商品
    // brandId=1&brandId=2
    private List<Long> brandId; // 按照品牌来查询 可以是多选
    // skuPrice=200_300
    // skuPrice=_300
    // skuPrice=200_
    private String skuPrice; // 价格区间查询
    // 不同的属性 attrs=20_8英寸:10英寸&attrs=19_64GB:32GB
    private List<String> attrs; // 按照属性信息进行筛选

    private Integer pageNum = 1; // 页码

}
