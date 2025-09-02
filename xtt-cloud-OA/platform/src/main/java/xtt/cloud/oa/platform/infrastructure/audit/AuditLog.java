package xtt.cloud.oa.platform.infrastructure.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sys_audit_log")
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String action;
    private String resource;
    private String method;
    private String ip;
    private String result;
    private String message;
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}


