package cn.itcast.core.controller;

import cn.itcast.core.pojo.entity.Result;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.PatternSyntaxException;

/**
 * 用户注册
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * 向指定手机号发送短信验证码
     * @param phone 手机号
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
            //1. 判断手机号是否合法
            boolean isCheck = PhoneFormatCheckUtils.isPhoneLegal(phone);
            if (!isCheck) {
                return new Result(false, "手机号不合法!");
            }
            //2. 调用发送短信服务接口
            userService.sendCode(phone);
            return new Result(true, "发送成功!");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return new Result(false, "发送失败!");
        }
    }

    /**
     * 用户注册, 保存用户
     * @param user      页面传入的用户对象
     * @param smscode   消费者页面输入的验证码
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode) {
        try {
            //1. 校验验证码是否正确
            if (smscode == null || "".equals(smscode)) {
                return new Result(false, "请填写验证码!");
            }
            boolean isCheck = userService.checkCode(user.getPhone(), smscode);
            if (!isCheck) {
                return new Result(false, "验证码填写错误!");
            }
            //2. 保存用户对象到数据库中完成注册
            userService.add(user);
            return new Result(true, "注册成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "注册失败!");
        }
    }
}
