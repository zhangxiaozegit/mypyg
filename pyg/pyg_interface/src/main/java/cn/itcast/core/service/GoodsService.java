package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.GoodsEntity;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.good.Goods;

public interface GoodsService {

    public PageResult search(Goods goods, Integer page, Integer rows);

    public void add(GoodsEntity goodsEntity);

    public GoodsEntity findOne(Long id);

    public  void  update(GoodsEntity goodsEntity);

    public void delete(Long id);

    public void updateStatus(Long[] ids, String status);
}
