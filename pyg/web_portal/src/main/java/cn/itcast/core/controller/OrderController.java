package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.service.BuyerCartService;
import cn.itcast.core.service.OrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单管理
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderService orderService;

    @Reference
    private BuyerCartService buyerCartService;

    /**
     * 提交订单
     * @param order  页面传入这个对象里面只有收货人地址, 联系人姓名, 手机号
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Order order) {
        try {
            //1. 获取当前登录用户的用户名
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            //2. 将当前用户的用户名放入页面传进来的order对象中
            order.setUserId(userName);
            //3. 通过登录用户的用户名获取这个人的购物车列表
            List<BuyerCart> cartList = buyerCartService.getCartListFromRedis(userName);
            //3. 保存订单
            orderService.add(order, cartList);
            return  new Result(true, "保存订单成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false, "保存订单失败!");
        }
    }
}
