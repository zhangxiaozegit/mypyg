package cn.itcast.core.service;

import cn.itcast.core.util.HttpClient;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    //微信公众账号或开放平台APP的唯一标识
    @Value("${appid}")
    private String appid;

    //财付通平台的商户账号
    @Value("${partner}")
    private String partner;

    //财付通平台的商户密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public Map createNative(String tradeNo, String totalFee) {
        //1.创建参数
        Map<String,String> param=new HashMap();//创建参数
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "品优购");//商品描述
        param.put("out_trade_no", tradeNo);//商户订单号
        param.put("total_fee",totalFee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://www.itcast.cn");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        try {
            //2.生成要发送的xml格式数据
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            //调用微信统一下单接口, 通过httpClient工具发送请求
            HttpClient client=new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //使用https协议发送
            client.setHttps(true);
            //发送的xml格式字符串数据
            client.setXmlParam(xmlParam);
            //使用post请求发送
            client.post();
            //3.发送请求并获得结果
            String result = client.getContent();
            System.out.println(result);
            //调用微信工具类, 将返回的xml格式字符串转换成map格式
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付链接
            map.put("total_fee", totalFee);//总金额
            map.put("out_trade_no",tradeNo);//订单号
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }

    }

    @Override
    public Map queryPayStatus(String tradeNo) {
        //封装发送的数据参数
        Map param=new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", tradeNo);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        //发送的地址
        String url="https://api.mch.weixin.qq.com/pay/orderquery";
        try {
            //调用微信工具类, 将封装的发送参数, 转换成xml格式字符串
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            //使用httpClient工具发送https请求, 到微信服务器
            HttpClient client=new HttpClient(url);
            //设置使用https协议
            client.setHttps(true);
            //设置发送的内容
            client.setXmlParam(xmlParam);
            //设置post请求发送
            client.post();
            //发送并返回结果, 结果为xml格式字符串
            String result = client.getContent();
            //调用微信工具类, 将返回的xml格式字符串转换成Map
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
