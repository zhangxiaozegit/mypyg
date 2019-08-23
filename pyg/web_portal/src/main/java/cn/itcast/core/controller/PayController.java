package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.service.OrderService;
import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付流程
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference
    private PayService payService;

    /**
     * 生成支付链接
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative() {
        //1. 获取当前登录用户的用户名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 根据当前登录用户的用户名, 到redis中获取未支付的日志记录
        PayLog payLog = orderService.findPayLogFromRedis(userName);
        //3. 根据日志对象中的支付单号和总金额调用, 微信统一下单接口, 生成支付链接返回
        //String.valueOf(payLog.getTotalFee())
        Map resultMap = payService.createNative(payLog.getOutTradeNo(), "1");
        return resultMap;
    }

    /**
     * 根据支付单号查询支付状态
     * @param out_trade_no  支付单号
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int flag = 1;
        //1. 死循环
        while(true) {
            //2. 根据支付单号, 调用微信的查询订单接口查询支付状态
            Map resultMap = payService.queryPayStatus(out_trade_no);
            //3. 如果返回空的对象表示支付超时
            if (resultMap == null) {
                result = new Result(false, "二维码超时");
                break;
            }
            //4. 如果返回数据中trade_state属性为SUCCESS代表支付成功
           if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                //更新支付状态到数据库, 并且清除缓存中的未支付日志
                orderService.updateOrderStatus(out_trade_no, String.valueOf(resultMap.get("transaction_id")));
                result = new Result(true, "支付成功!");
                break;
           }
            //超过20分钟未支付, 则支付超时
            if (flag >= 400) {
                result = new Result(false, "二维码超时");
                break;
            }
            try {
                //5. 每查询一次让线程睡3秒, 防止查询太频繁, 服务器压力大
                Thread.sleep(3000);
                flag++;
            } catch (Exception e) {
                e.printStackTrace();
                result = new Result(false, "二维码超时");
            }
        }
        return result;
    }
}
