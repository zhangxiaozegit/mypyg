package cn.itcast.core.service;

import java.util.Map;

public interface PageService {

    /**
     * 根据商品id, 获取生成静态页面所需要的所有数据
     * 包括, 商品, 商品详情, 库存集合, 分类数据
     */
    public Map<String, Object> findGoodsData(Long goodsId);

    /**
     * 生成静态化页面
     * @param goodsId   商品id
     * @param rootMap   模板中需要的数据
     */
    public void createStaticPage(Long goodsId, Map<String, Object> rootMap)throws Exception ;
}
