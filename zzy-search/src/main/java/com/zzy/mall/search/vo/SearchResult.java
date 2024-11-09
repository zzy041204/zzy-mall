package com.zzy.mall.search.vo;

import com.zzy.mall.common.dto.es.SkuESModel;
import lombok.Data;

import java.util.List;

/**
 * 封装检索后的响应信息
 */
@Data
public class SearchResult {

    private List<SkuESModel> products; // 查询到满足条件的所有商品信息

    private Integer pageNum; // 当前页
    private Long total; // 总的记录数
    private Integer totalPages; // 总页数

    private List<Integer> navs; // 需要显示的分页的页码

    private List<BrandVO> brands; // 当前查询的所有商品涉及到的所有品牌信息

    private List<AttrVO> attrs; // 当前查询的所有的商品涉及到的属性信息

    private List<CatalogVO> catalogs; //当前查询到的所有商品涉及到的类别信息

    @Data
    public static class CatalogVO{
        private Long categoryId;
        private String categoryName;
    }

    /**
     * 品牌的相关信息
     */
    @Data
    public static class BrandVO{
        private Long brandId; // 品牌的编号
        private String brandName; // 品牌的名称
        private String brandImg; // 品牌的图片
    }

    /**
     * 属性相关的信息
     */
    @Data
    public static class AttrVO{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

}
