package xtt.cloud.oa.file.domain;

import java.util.List;
import java.util.Optional;

/**
 * 文件存储接口
 * 
 * @author xtt
 */
public interface FileStorage {
    
    /**
     * 保存文件信息
     * 
     * @param fileInfo 文件信息
     */
    void save(FileInfo fileInfo);
    
    /**
     * 根据文件ID查找文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    Optional<FileInfo> findByFileId(String fileId);
    
    /**
     * 根据用户ID查找文件列表
     * 
     * @param userId 用户ID
     * @return 文件列表
     */
    List<FileInfo> findByUploadUserId(Long userId);
    
    /**
     * 删除文件信息
     * 
     * @param fileId 文件ID
     */
    void delete(String fileId);
}

