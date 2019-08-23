package cn.itcast.core.service;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService, ServletContextAware {

    @Autowired
    private GoodsDao goodsDao;

    @Autowired
    private GoodsDescDao descDao;

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemCatDao catDao;

    @Autowired
    private FreeMarkerConfigurer freemarkerConfig;

    private ServletContext servletContext;

    @Override
    public Map<String, Object> findGoodsData(Long goodsId) {
        Map<String, Object> rootMap = new HashMap<>();
        //1. 根据商品id获取商品对象
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);

        //2. 根据商品id获取商品详情对象
        GoodsDesc goodsDesc = descDao.selectByPrimaryKey(goodsId);

        //3. 根据商品id获取库存集合对象
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(goodsId);
        List<Item> itemList = itemDao.selectByExample(query);

        //4. 根据商品中的对应的分类id, 找到对应的分类对象
        if (goods != null) {
            String itemCat1 = catDao.selectByPrimaryKey(goods.getCategory1Id()).getName();
            rootMap.put("itemCat1", itemCat1);
            String itemCat2 = catDao.selectByPrimaryKey(goods.getCategory2Id()).getName();
            rootMap.put("itemCat2", itemCat2);
            String itemCat3 = catDao.selectByPrimaryKey(goods.getCategory3Id()).getName();
            rootMap.put("itemCat3", itemCat3);
        }
        //5. 将查询出来的数据封装后返回
        rootMap.put("goods", goods);
        rootMap.put("goodsDesc", goodsDesc);
        rootMap.put("itemList", itemList);
        return rootMap;
    }

    @Override
    public void createStaticPage(Long goodsId, Map<String, Object> rootMap) throws Exception {
        //1. 获取模板初始化对象
        Configuration conf = freemarkerConfig.getConfiguration();
        //2. 加载模板获取模板对象
        Template template = conf.getTemplate("item.ftl");
        //3. 设置静态页面生成后的位置, 以及生成后的页面名称
        String path = goodsId + ".html";
        //将相对路径转化成绝对路径
        String realPath = getRealPath(path);
        System.out.println("====realPath=====" + realPath);
        //4. 创建输出流
        Writer out = new OutputStreamWriter(new FileOutputStream(new File(realPath)), "utf-8");
        //5. 生成
        template.process(rootMap, out);
        //6. 关闭流
        out.close();
    }

    /**
     * 将相对路径转换成绝对路径
     * @param path  相对路径    例如: xxxxxx.html
     * @return 绝对路径    例如: d://xxxx/xxx/xxx/xxxx.html
     */
    private String getRealPath(String path) {
        String realPath = servletContext.getRealPath(path);
        return realPath;
    }

    /**
     * spring框架初始化了ServletContextAware接口, ServletContextAware接口中的servletContext对象
     * 被spring实例化了, 我们实现ServletContextAware目的就是使用里面实例化好的servletContext对象给我们当前类的
     * servletContext对象赋值, 也就相当于实例化当前类的servletContext对象
     * @param servletContext
     */
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
