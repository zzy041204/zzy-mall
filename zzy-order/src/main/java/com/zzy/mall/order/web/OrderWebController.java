package com.zzy.mall.order.web;

import com.zzy.mall.common.constant.OrderConstant;
import com.zzy.mall.common.exception.NoStockException;
import com.zzy.mall.common.exception.RepeatSubmitException;
import com.zzy.mall.order.config.AliPayTemplate;
import com.zzy.mall.order.interceptor.AuthInterceptor;
import com.zzy.mall.order.service.OrderService;
import com.zzy.mall.order.vo.OrderConfirmVO;
import com.zzy.mall.order.vo.OrderResponseVO;
import com.zzy.mall.order.vo.OrderSubmitVO;
import com.zzy.mall.order.vo.PayVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @Autowired
    AliPayTemplate aliPayTemplate;

    @GetMapping("/toTrade")
    public String toTrace(Model model){
        // 查询订单页确认需要的信息
        OrderConfirmVO vo = null;
        try {
            vo = orderService.confirmOrder();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("confirmVO",vo);
        return "confirm";
    }

    @PostMapping("/orderSubmit")
    public String orderSubmit(OrderSubmitVO vo, Model model, RedirectAttributes redirectAttributes){
        OrderResponseVO responseVO = null;
        Integer code = 0;
        try {
            responseVO = orderService.submitOrder(vo);
            code = responseVO.getCode();
        }catch (NoStockException e){
            e.printStackTrace();
            code = 2;
        }catch (RepeatSubmitException e){
            e.printStackTrace();
            code = 1;
        }
        if (code == 0){
            // 表示下单操作成功
            model.addAttribute("orderResponseVO",responseVO);
            return "pay";
        }else {
            if (code == 1){
                System.out.println("重复提交");
                redirectAttributes.addFlashAttribute("msg","重复提交");
            }else {
                System.out.println("库存不足");
                redirectAttributes.addFlashAttribute("msg","库存不足");
            }
            // 表示下单操作失败
            return "redirect:http://order.zzy.com/toTrade";
        }
    }

    @GetMapping("/orderPay/returnUrl")
    public String orderPay(@RequestParam(value = "orderSn",required = false) String orderSn,
                           @RequestParam(value = "out_trade_no",required = false) String out_trade_no){
        // 完成相关的支付操作
        if (StringUtils.isNotBlank(orderSn)){
            orderService.handleOrderComplete(orderSn);
        }else {
            orderService.handleOrderComplete(out_trade_no);
        }
        System.out.println(orderSn);
        return "list";
    }

    /**
     * 获取订单相关信息
     * 跳转到支付页面
     * @param orderSn
     * @return
     */
    @GetMapping(value = "/payOrder",produces = "text/html")
    @ResponseBody
    public String payOrder(@RequestParam("orderSn") String orderSn){
        // 根据订单编号查询相关的订单信息 封装到PayVO中
        PayVO payVO = orderService.getOrderPay(orderSn);
        String pay = aliPayTemplate.pay(payVO);
        //System.out.println(pay);
        return pay;
    }

}
