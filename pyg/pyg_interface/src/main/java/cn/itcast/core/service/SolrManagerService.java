package cn.itcast.core.service;

public interface SolrManagerService {

    //商品上架使用
    public void addItemToSolr(Long goodsId);

    //商品下架使用
    public void deleteItemByGoodsId(Long goodsId);
}
