package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
    private SellerService sellerService;

    /**
     * 商家注册
     * @param seller    商家实体
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller) {
        try {
            //密码加密
//            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//            String password = passwordEncoder.encode(seller.getPassword());
//            seller.setPassword(password);

            sellerService.add(seller);
            return new Result(true, "注册成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败!");
        }
    }
}
