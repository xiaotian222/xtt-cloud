package xtt.cloud.oa.common.infra.designpattern.factorymethod;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/20
 * 描述: 这里是对代码的描述
 */

interface Product {
    String name();
}
class AProduct implements Product {
    public String name() {
        return "A";
    }
}
class BProduct implements Product {
    public String name() {
        return "B";
    }
}

abstract class Creator {
    abstract Product create();
}
class ACreator extends Creator {
    Product create() {
        return new AProduct();
    }
}
class BCreator extends Creator {
    Product create() {
        return new BProduct();
    }
}

class FactoryMethodDemo {
    public static void main(String[] args) {
        Creator c = Math.random() > 0.5 ? new ACreator() : new BCreator();
        System.out.println(c.create().name());
    }
}

public class fm {


}
