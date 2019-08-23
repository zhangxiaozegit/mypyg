package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import cn.itcast.core.service.PageService;
import cn.itcast.core.service.SolrManagerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.management.relation.RelationSupport;
import java.util.Map;

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
//
//    @Reference
//    private PageService pageService;

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

    @RequestMapping("/findOne")
    public GoodsEntity findOne(Long id) {
        GoodsEntity one = goodsService.findOne(id);
        return one;
    }

    /**
     * 商品状态修改
     * @param ids       商品id数组
     * @param status    状态码, 0未审核, 1审核通过, 2驳回
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            //1. 到数据库中更新商品的审核状态
            goodsService.updateStatus(ids, status);
            //2. 如果审核通过,
//            if ("1".equals(status) && ids != null) {
//                for (Long goodsId : ids) {
//                    //3. 根据商品id查询数据库商品的详细数据, 然后放入solr索引库中供搜索使用
//                    solrManagerService.addItemToSolr(goodsId);
//                    //4. 根据商品id获取商品详细数据, 根据模板生成商品详情静态化页面
//                    Map<String, Object> rootMap = pageService.findGoodsData(goodsId);
//                    pageService.createStaticPage(goodsId, rootMap);
//                }
//            }
            return new Result(true, "状态修改成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "状态修改失败!");
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

    /**
     * 测试生成静态化页面
     * @param goodsId   商品id
     * @return
     */
//    @RequestMapping("/page")
//    public Result testCreateStaticPage(Long goodsId) {
//        try {
//            Map<String, Object> rootMap = pageService.findGoodsData(goodsId);
//            pageService.createStaticPage(goodsId, rootMap);
//            return new Result(true, "生成成功!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new Result(false, "生成失败!");
//        }
//    }

}
