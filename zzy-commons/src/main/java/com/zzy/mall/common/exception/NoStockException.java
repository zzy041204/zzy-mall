package com.zzy.mall.common.exception;

public class NoStockException extends RuntimeException{

    private Long skuId;

    public NoStockException(Long skuId){
        super("当前商品["+skuId+"]无库存");
        this.skuId=skuId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
