package xtt.cloud.oa.platform.infrastructure.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = Logger.getLogger("AUDIT");

    @Pointcut("within(xtt.cloud.oa.platform.interfaces.rest..*)")
    public void restLayer() {}

    private final AuditLogRepository repository;

    public AuditAspect(AuditLogRepository repository) {
        this.repository = repository;
    }

    @AfterReturning(pointcut = "restLayer()")
    public void after(JoinPoint jp) {
        // 简易入库
        AuditLog al = new AuditLog();
        al.setAction("invoke");
        al.setResource(jp.getSignature().toShortString());
        al.setMethod("HTTP");
        al.setResult("SUCCESS");
        repository.save(al);
        log.info(() -> "AUDIT SAVED -> " + jp.getSignature());
    }
}


