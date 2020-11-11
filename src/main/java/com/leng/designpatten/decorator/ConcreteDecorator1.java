package com.leng.designpatten.decorator;

/**
 * @Classname ConcreteDecorator1
 * @Date 2020/11/11 23:52
 * @Autor lengxuezhang
 */
public class ConcreteDecorator1 extends Decorator {

    // 定义被修饰者，通过构造函数传入
    public ConcreteDecorator1(Component _component) {
        super(_component);
    }
    // 定义自己的方法，即要增强的功能
    private void method1() {
        System.out.println("method1 修饰");
    }

    // 重写父类的方法
    @Override
    public void operate() {
        this.method1();
        super.operate();
    }
}
