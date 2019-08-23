package cn.itcast.core.service;

import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;

public interface SellerService {

    public PageResult findPage(Integer page, Integer rows, Seller seller);

    public void add(Seller seller);

    public Seller findOne(String id);

    public void updateStatus(String sellerId, String status);
}
