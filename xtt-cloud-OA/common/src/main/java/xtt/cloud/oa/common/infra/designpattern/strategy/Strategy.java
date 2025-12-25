package xtt.cloud.oa.common.infra.designpattern.strategy;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/20
 * 描述: 这里是对代码的描述
 */

interface PayStrategy {
    void pay(int amount);
}

class AliPay implements PayStrategy {
    public void pay(int a) {
        System.out.println("Ali:" + a);
    }
}

class WechatPay implements PayStrategy {
    public void pay(int a) {
        System.out.println("Wx:" + a);
    }
}

class Context {
    PayStrategy  payStrategy;

    public Context(PayStrategy payStrategy){
        this.payStrategy = payStrategy;
    }

    public void execute(int a){
        payStrategy.pay(a);
    }
}

public class Strategy {

}

class StrategyDemo {
    public static void main(String[] a) {

        Context context = new Context(new AliPay());
        context.execute(1);


    }
}