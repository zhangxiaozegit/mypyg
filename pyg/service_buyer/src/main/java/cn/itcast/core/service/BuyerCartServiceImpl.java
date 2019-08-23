package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.entity.BuyerCart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.util.Constants;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class BuyerCartServiceImpl implements BuyerCartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ItemDao itemDao;

    @Override
    public List<BuyerCart> addItemToCartList(List<BuyerCart> cartList, Long itemId, Integer num) {
        //1. 根据商品SKU ID查询SKU商品信息
        Item item = itemDao.selectByPrimaryKey(itemId);
        //2. 判断商品是否存在不存在, 抛异常
        if (item == null) {
            throw new RuntimeException("您购买的商品不存在!");
        }
        //3. 判断商品状态是否为1已审核, 状态不对抛异常
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("您购买的商品非法!");
        }
        //4.获取商家ID
        String sellerId = item.getSellerId();
        //5.根据商家ID查询购物车列表中是否存在该商家的购物车
        BuyerCart buyerCart = findCartBySellerId(sellerId, cartList);
        //6.判断如果购物车列表中不存在该商家的购物车
        if (buyerCart == null) {
            //6.a.1 新建购物车对象
            buyerCart = new BuyerCart();
            //向购物车中加入卖家id
            buyerCart.setSellerId(sellerId);
            //向购物车中加入卖家名称
            buyerCart.setSellerName(item.getSeller());
            //创建购物车中需要的购物项集合
            List<OrderItem> orderItemList = new ArrayList<>();
            //创建购物项对象
            OrderItem orderItem = createOrderItem(item, num);
            //购物项加入购物项集合中
            orderItemList.add(orderItem);
            //向购物车中加入购物项集合
            buyerCart.setOrderItemList(orderItemList);
            //6.a.2 将新建的购物车对象添加到购物车列表
            cartList.add(buyerCart);
        } else {
            //6.b.1如果购物车列表中存在该商家的购物车 (查询购物车明细列表中是否存在该商品)
            List<OrderItem> orderItemList = buyerCart.getOrderItemList();
            OrderItem orderItem = findOrderItemByItemId(orderItemList, itemId);
            //6.b.2判断购物车明细是否为空
            if (orderItem  == null) {
                //6.b.3为空，新增购物车明细
                orderItem = createOrderItem(item, num);
                //将新建的购物项对象放入购物项集合中
                orderItemList.add(orderItem);
            } else {
                //6.b.4不为空，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(orderItem.getNum())));
            }
            //6.b.5如果购物车明细中数量操作后小于等于0，则移除
            if (orderItem.getNum() <= 0) {
                orderItemList.remove(orderItem);
            }
            //6.b.6如果购物车中购物车明细列表为空,则移除
            if (orderItemList.size() == 0) {
                cartList.remove(buyerCart);
            }
        }
        //7. 返回购物车列表对象
        return cartList;
    }

    /**
     * 从购物项集合中找符合itemId的同款购物项
     * @param orderItemList 购物项集合
     * @param itemId        库存id
     * @return
     */
    private OrderItem findOrderItemByItemId(List<OrderItem> orderItemList, Long itemId) {
        if (orderItemList != null) {
            for (OrderItem orderItem : orderItemList) {
                if (itemId.equals(orderItem.getItemId())) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    /**
     * 创建购物项对象
     * @param item  库存对象
     * @param num   购买商品数量
     * @return
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        if (num == null || num <= 0) {
            throw new RuntimeException("您购买数量非法!");
        }
        OrderItem orderItem = new OrderItem();
        //库存标题
        orderItem.setTitle(item.getTitle());
        //购买数量
        orderItem.setNum(num);
        //卖家id
        orderItem.setSellerId(item.getSellerId());
        //商品单价
        orderItem.setPrice(item.getPrice());
        //示例图片地址
        orderItem.setPicPath(item.getImage());
        //库存id
        orderItem.setItemId(item.getId());
        //商品id
        orderItem.setGoodsId(item.getGoodsId());
        //总价 = 单价 * 购买数量
        orderItem.setTotalFee(orderItem.getPrice().multiply(new BigDecimal(num)));
        return orderItem;
    }

    /**
     * 从购物车列表中获取这个卖家的购物车
     * @param sellerId  卖家id
     * @param cartList  购物车列表
     * @return
     */
    private BuyerCart findCartBySellerId(String sellerId, List<BuyerCart> cartList) {
        if (cartList != null) {
            for (BuyerCart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }

    @Override
    public void setCartListToRedis(String userName, List<BuyerCart> cartList) {
        redisTemplate.boundHashOps(Constants.REDIS_CARTLIST).put(userName, cartList);
    }

    @Override
    public List<BuyerCart> getCartListFromRedis(String userName) {
        List<BuyerCart> cartList = (List<BuyerCart>)redisTemplate.boundHashOps(Constants.REDIS_CARTLIST).get(userName);
        if (cartList == null) {
            cartList = new ArrayList<BuyerCart>();
        }
        return cartList;
    }

    @Override
    public List<BuyerCart> mergeCookieCartListToRedisCartList(List<BuyerCart> cookieCartList, List<BuyerCart> redisCartList) {
        if (cookieCartList != null) {
            //遍历cookie购物车集合
            for (BuyerCart cart : cookieCartList) {
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null) {
                    //遍历cookie中的购物项集合
                    for (OrderItem orderItem : orderItemList) {
                        //向redis购物车集合中加入cookie中的购物项
                        redisCartList = addItemToCartList(redisCartList, orderItem.getItemId(), orderItem.getNum());
                    }
                }
            }
        }
        return redisCartList;
    }
}
