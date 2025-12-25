package xtt.cloud.oa.common.infra.designpattern.singleton;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/20
 * 描述: 这里是对代码的描述
 */

public class SingletonObject {

    private static volatile SingletonObject singletonObject = null;

    private SingletonObject() {}

    public static SingletonObject getInstance(){
        if(singletonObject == null){
            synchronized (SingletonObject.class){
                //双重检查
                if(singletonObject == null){
                    singletonObject = new SingletonObject();
                }
            }
        }
        return singletonObject;
    }
}
