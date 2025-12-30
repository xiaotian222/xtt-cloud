package xtt.cloud.oa.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传会话信息
 * 
 * @author xtt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadSession {
    
    /**
     * 上传ID
     */
    private String uploadId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 文件总大小（字节）
     */
    private Long totalSize;
    
    /**
     * 分片大小（字节）
     */
    private Long chunkSize;
    
    /**
     * 总分片数
     */
    private Integer totalChunks;
    
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
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 已上传的分片索引列表
     */
    private List<Integer> uploadedChunks;
    
    /**
     * 是否已完成
     */
    private Boolean completed;
    
    /**
     * 最终文件ID（合并完成后）
     */
    private String fileId;
}

