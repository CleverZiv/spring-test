package com.leng.designpatten.decorator;

/**
 * @Classname ConcreteComponent
 * @Date 2020/11/11 23:48
 * @Autor lengxuezhang
 */
public class ConcreteComponent extends Component {
    @Override
    public void operate() {
        System.out.println("do something");
    }
}
