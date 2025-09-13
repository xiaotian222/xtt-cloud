package xtt.cloud.oa.platform.infrastructure.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import xtt.cloud.oa.platform.domain.mapper.AuditLogMapper;

import java.time.LocalDateTime;
import java.util.logging.Logger;

@Aspect
@Component
public class AuditAspect {
    private static final Logger log = Logger.getLogger("AUDIT");

    @Pointcut("within(xtt.cloud.oa.platform.interfaces.rest..*)")
    public void restLayer() {}

    private final AuditLogMapper auditLogMapper;

    public AuditAspect(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @AfterReturning(pointcut = "restLayer()")
    public void after(JoinPoint jp) {
        // 简易入库
        AuditLog al = new AuditLog();
        al.setAction("invoke");
        al.setResource(jp.getSignature().toShortString());
        al.setMethod("HTTP");
        al.setResult("SUCCESS");
        al.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(al);
        log.info(() -> "AUDIT SAVED -> " + jp.getSignature());
    }
}


