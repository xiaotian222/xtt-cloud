package xtt.cloud.oa.file.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分片信息
 * 
 * @author xtt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkInfo {
    
    /**
     * 上传ID（同一文件的所有分片共享同一个 uploadId）
     */
    private String uploadId;
    
    /**
     * 分片索引（从 0 开始）
     */
    private Integer chunkIndex;
    
    /**
     * 分片大小（字节）
     */
    private Long chunkSize;
    
    /**
     * 分片存储路径
     */
    private String chunkPath;
    
    /**
     * 分片 MD5 值（可选，用于校验）
     */
    private String md5;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;
}

