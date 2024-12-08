package com.zzy.mall.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.zzy.mall.common.utils.R;
import com.zzy.mall.seckill.dto.SeckillSkuRedisDTO;
import com.zzy.mall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @GetMapping("/currentSeckillSessionSkus")
    @ResponseBody
    public R getCurrentSeckillSessionSkus(){
        List<SeckillSkuRedisDTO> currentSeckillSkus = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", JSON.toJSONString(currentSeckillSkus));
    }

    @GetMapping("/seckillSessionBySkuId")
    @ResponseBody
    public R getSeckillSessionBySkuId(@RequestParam("skuId") Long skuId){
        SeckillSkuRedisDTO dto = seckillService.getSeckillSessionBySkuId(skuId);
        return R.ok().put("data",JSON.toJSONString(dto));
    }

    /**
     * 秒杀抢购
     * @param killId
     * @param code
     * @param num
     * @return
     */
    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") String killId,
                     @RequestParam("code") String code,
                     @RequestParam("num") Integer num,
                     Model model){
        String orderSn = seckillService.kill(killId,code,num);
        model.addAttribute("orderSn",orderSn);
        return "success";
    }

}
