package xtt.cloud.oa.workflow.infrastructure.lock;

/**
 * 锁获取失败异常
 * 
 * @author xtt
 */
public class LockAcquisitionException extends RuntimeException {
    
    public LockAcquisitionException(String message) {
        super(message);
    }
    
    public LockAcquisitionException(String message, Throwable cause) {
        super(message, cause);
    }
}

