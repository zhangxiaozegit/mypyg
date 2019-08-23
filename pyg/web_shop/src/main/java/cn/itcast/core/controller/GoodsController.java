package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品管理
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

//    @Reference
//    private SolrManagerService solrManagerService;

    /**
     * 商品分页查询
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody Goods goods, Integer page, Integer rows) {
        //1. 获取当前登录用户的用户名
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        //2. 向查询条件对象中添加当前登录用户的用户名作为查询条件
        goods.setSellerId(userName);
        //3. 进行分页查询
        PageResult pageResult = goodsService.search(goods, page, rows);
        return pageResult;
    }

    /**
     * 商品添加
     * @param goodsEntity 商品实体, 包含商品对象, 商品详情对象, 库存集合对象
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsEntity goodsEntity) {
        try {
            //1. 获取当前登录用户的用户名
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            //2. 将用户身份信息放入商品对象中
            goodsEntity.getGoods().setSellerId(userName);
            //3. 保存
            goodsService.add(goodsEntity);
            return new Result(true, "添加成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败!");
        }
    }

    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        GoodsEntity one = goodsService.findOne(id);
        return one;
    }

    @RequestMapping("/update")
    public Result update(@RequestBody GoodsEntity goodsEntity) {
        try {
            //1. 获取当前登录用户的用户名
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();
            //2. 对比传入的商品的用户是否为当前账户添加的商品
            if (!userName.equals(goodsEntity.getGoods().getSellerId())) {
                //3. 如果不是当前用户添加的商品不允许修改
                return new Result(false, "您没有权限修改此商品!");
            }

            //4. 修改保存
            goodsService.update(goodsEntity);
            return  new Result(true, "修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false, "修改失败!");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            if (ids != null) {
                for (Long goodsId : ids) {
                    //1. 到数据库中根据商品id, 逻辑删除商品数据
                    goodsService.delete(goodsId);
                    //2.根据商品id删除solr索引库中的库存数据
                    //solrManagerService.deleteItemByGoodsId(goodsId);
                }
            }
            return new Result(true, "删除成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败!");
        }
    }
}
