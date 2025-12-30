package xtt.cloud.oa.workflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Workflow 配置属性
 * 
 * @author XTT Cloud
 */
@ConfigurationProperties(prefix = "xtt.workflow")
public class WorkflowProperties {
    
    /**
     * 是否启用 workflow 功能
     */
    private boolean enabled = true;
    
    /**
     * 是否启用默认 REST API
     */
    private boolean enableWebApi = true;
    
    /**
     * MyBatis Mapper 扫描包路径
     */
    private String mapperScanPackage = "xtt.cloud.oa.workflow.infrastructure.persistence.mapper";
    
    /**
     * Feign 客户端扫描包路径
     */
    private String feignClientScanPackage = "xtt.cloud.oa.workflow.infrastructure.external.client";
    
    /**
     * 是否启用异步支持
     */
    private boolean enableAsync = true;
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isEnableWebApi() {
        return enableWebApi;
    }
    
    public void setEnableWebApi(boolean enableWebApi) {
        this.enableWebApi = enableWebApi;
    }
    
    public String getMapperScanPackage() {
        return mapperScanPackage;
    }
    
    public void setMapperScanPackage(String mapperScanPackage) {
        this.mapperScanPackage = mapperScanPackage;
    }
    
    public String getFeignClientScanPackage() {
        return feignClientScanPackage;
    }
    
    public void setFeignClientScanPackage(String feignClientScanPackage) {
        this.feignClientScanPackage = feignClientScanPackage;
    }
    
    public boolean isEnableAsync() {
        return enableAsync;
    }
    
    public void setEnableAsync(boolean enableAsync) {
        this.enableAsync = enableAsync;
    }
}

