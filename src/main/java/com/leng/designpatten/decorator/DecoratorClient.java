package com.leng.designpatten.decorator;

/**
 * @Classname DecoratorClient
 * @Date 2020/11/11 23:56
 * @Autor lengxuezhang
 */
public class DecoratorClient {
    public static void main(String[] args) {
        // 初始化具体实现类对象
        Component component = new ConcreteComponent();
        // 第一次装饰
        component = new ConcreteDecorator1(component);
        // 第二次装饰
        component = new ConcreteDecorator2(component);
        component.operate();
    }
}