package com.leng.designpatten.decorator;

/**
 * @Classname Decorator
 * @Date 2020/11/11 23:49
 * @Autor lengxuezhang
 */
public abstract class Decorator extends Component{
    private Component component = null;

    public Decorator (Component _component) {
        this.component = _component;
    }

    @Override
    public void operate() {
        this.component.operate();
    }

}
