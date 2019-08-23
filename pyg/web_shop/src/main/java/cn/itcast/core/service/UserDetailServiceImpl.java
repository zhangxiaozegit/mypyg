package cn.itcast.core.service;

import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义登录业务
 */
public class UserDetailServiceImpl implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //创建权限集合
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        //向权限集合中加入访问权限
        authList.add(new SimpleGrantedAuthority("ROLE_SELLER"));

        //1. 判断用户名不为空
        if (username != null) {
            //2. 根据用户名到数据获取卖家对象
            Seller seller = sellerService.findOne(username);
            //3. 判断卖家对象不为空
            if (seller != null) {
                //4. 判断审核状态为审核通过
                if ("1".equals(seller.getStatus())) {
                    //4. 返回springSecurity中需要的User对象
                    return new User(username, seller.getPassword(), authList);
                }
            }
        }

        return null;
    }
}
