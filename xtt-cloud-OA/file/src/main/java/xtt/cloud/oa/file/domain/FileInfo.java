package xtt.cloud.oa.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件信息
 * 
 * @author xtt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    
    /**
     * 文件ID
     */
    private String fileId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 存储文件名
     */
    private String filename;
    
    /**
     * 存储路径（相对路径）
     */
    private String storagePath;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String extension;
    
    /**
     * 上传用户ID
     */
    private Long uploadUserId;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}

