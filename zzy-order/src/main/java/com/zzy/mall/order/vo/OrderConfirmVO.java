package com.zzy.mall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单确认页面中的数据vo
 */
public class OrderConfirmVO {

    // 订单的收货人 及 收获地址信息
    @Getter @Setter
    private List<MemberAddressVO> address;
    // 购物车选中的商品信息
    @Getter @Setter
    private List<OrderItemVO> items;
    // 支付方式
    // 发票信息
    // 优惠信息

    @Getter @Setter
    private String orderToken;

    //private Integer countNum;

    public Integer getCountNum() {
        int count = 0;
        for (OrderItemVO item : items) {
            count += item.getCount();
        }
        return count;
    }

    private BigDecimal total; // 总的金额

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal(0);
        if (items != null && items.size() > 0) {
            for (OrderItemVO item : items) {
                sum = sum.add(item.getPrice().multiply(new BigDecimal(item.getCount())));
            }
        }
        return sum;
    }

    //private BigDecimal payTotal; // 需要支付的总金额

    public BigDecimal getPayTotal() {
        return getTotal();
    }

}
