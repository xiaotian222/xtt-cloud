package xtt.cloud.oa.common.designpattern.absfactory;

/**
 * 作者: HuTianRui
 * 日期: 2025/12/20
 * 描述: 这里是对代码的描述
 */

public abstract class AbstractFactory {

    public abstract Comment createComment();

    public abstract Document createDocument();


}

class CommentFactory extends AbstractFactory {
    @Override
    public Comment createComment() {

        return null;
    }

    @Override
    public Document createDocument() {

        return null;
    }
}

class DocumentFactory extends AbstractFactory {
    @Override
    public Comment createComment() {
        return null;
    }

    @Override
    public Document createDocument() {
        return null;
    }

}