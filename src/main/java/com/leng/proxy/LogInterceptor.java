package com.leng.proxy;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Date;

/**
 * @Classname LogInterceptor
 * @Date 2020/9/22 21:59
 * @Autor lengxuezhang
 */
public class LogInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        before();
        Object result = methodProxy.invokeSuper(o, objects);
        after();
        return result;
    }

    // 预处理方法
    private void before() {
        System.out.println(String.format("log start time [%s] ", new Date()));
    }

    // 后处理方法
    private void after() {
        System.out.println(String.format("log end time [%s] ", new Date()));
    }
}
