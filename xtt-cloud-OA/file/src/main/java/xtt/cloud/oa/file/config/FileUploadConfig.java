package xtt.cloud.oa.file.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * 文件上传配置
 * 
 * @author xtt
 */
@Configuration
public class FileUploadConfig {
    
    @Value("${file.upload.max-file-size:104857600}") // 默认 100MB
    private long maxFileSize;
    
    @Value("${file.upload.max-request-size:209715200}") // 默认 200MB
    private long maxRequestSize;
    
    @Bean
    public MultipartResolver multipartResolver() {
        StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
        return resolver;
    }
}

