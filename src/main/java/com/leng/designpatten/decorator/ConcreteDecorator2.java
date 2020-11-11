package com.leng.designpatten.decorator;

/**
 * @Classname ConcreteDecorator1
 * @Date 2020/11/11 23:52
 * @Autor lengxuezhang
 */
public class ConcreteDecorator2 extends Decorator {

    // 定义被修饰者，通过构造函数传入
    public ConcreteDecorator2(Component _component) {
        super(_component);
    }
    // 定义自己的方法，即要增强的功能
    private void method2() {
        System.out.println("method2 修饰");
    }

    // 重写父类的方法
    @Override
    public void operate() {
        this.method2();
        super.operate();
    }
}
