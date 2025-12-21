package xtt.cloud.oa.common.designpattern.factory;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/18
 * 描述: 这里是对代码的描述
 */

public class DocumentFactory {

    public DocumentFactory() {}

    //简单工厂
    public Document createDocument(String typeName) throws Exception {
        if ("Fawen".equals(typeName)) {
            return new Fawen();
        }else if ("Shouwen".equals(typeName)) {
            return new Shouwen();
        }else {
            throw new Exception("未找到的类型");
        }
    }


}
