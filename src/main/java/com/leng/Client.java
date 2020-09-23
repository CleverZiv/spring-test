package com.leng;


import com.leng.proxy.LogInterceptor;
import com.leng.proxy.UserServiceImpl;
import org.springframework.cglib.proxy.Enhancer;

/**
 * @Classname Client
 * @Date 2020/9/12 2:40
 * @Autor lengxuezhang
 */
public class Client {
    public static void main(String[] args) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserServiceImpl.class);
        enhancer.setCallback(new LogInterceptor());

        UserServiceImpl userService = (UserServiceImpl) enhancer.create();
        userService.login();
        userService.sayHello();
    }
}
