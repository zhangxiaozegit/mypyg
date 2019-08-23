package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 实现springSecurity的UserDetailsService接口, 进入到这里的请求 都是已经经过CAS单点登录服务器登陆过的
 * 在这里获取这个用户具有哪些访问权限集合, 封装成SpringSecurity需要的User对象, 返回给SpringSecurity.
 */
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建权限集合
        List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        //向权限集合中加入访问权限
        authList.add(new SimpleGrantedAuthority("ROLE_USER"));
        //封装springSecurity需要的User对象返回.
        return new User(username, "", authList);
    }
}
