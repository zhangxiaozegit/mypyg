package cn.itcast.core.service;

import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.entity.PageResult;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.seller.SellerQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    @Autowired
    private SellerDao sellerDao;

    @Override
    public PageResult findPage(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page, rows);
        //创建查询对象
        SellerQuery query = new SellerQuery();
        //创建where条件对象
        SellerQuery.Criteria criteria = query.createCriteria();
        if (seller != null) {
            if (seller.getStatus() != null && !"".equals(seller.getStatus())) {
                criteria.andStatusEqualTo(seller.getStatus());
            }
            if (seller.getName() != null && !"".equals(seller.getName())) {
                criteria.andNameLike("%"+seller.getName()+"%");
            }
        }
        Page<Seller> sellerList = (Page<Seller>)sellerDao.selectByExample(query);
        return new PageResult(sellerList.getTotal(), sellerList.getResult());
    }

    @Override
    public void add(Seller seller) {
        //新注册的用户, 审核状态默认为0, 未审核
        seller.setStatus("0");
        //创建时间
        seller.setCreateTime(new Date());
        sellerDao.insertSelective(seller);
    }

    @Override
    public Seller findOne(String id) {
        return sellerDao.selectByPrimaryKey(id);
    }

    @Override
    public void updateStatus(String sellerId, String status) {
        Seller seller = new Seller();
        //主键id
        seller.setSellerId(sellerId);
        //状态
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
