package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;

import java.util.List;

public interface OrderService {

    public void add(Order order, List<BuyerCart> cartList);

    /**
     * 根据用户名, 获取redis中的未支付日志记录
     * @param userName
     * @return
     */
    public PayLog findPayLogFromRedis(String  userName);


    /**
     * 支付成功后更新支付状态
     * @param out_trade_no      支付单号, payLog支付日志的主键
     * @param transaction_id    交易号, 微信给我们返回的
     */
    public void updateOrderStatus(String out_trade_no,String transaction_id);
}
