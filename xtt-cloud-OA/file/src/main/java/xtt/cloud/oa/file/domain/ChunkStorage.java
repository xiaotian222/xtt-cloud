package xtt.cloud.oa.file.domain;

import java.util.List;
import java.util.Optional;

/**
 * 分片存储接口
 * 
 * @author xtt
 */
public interface ChunkStorage {
    
    /**
     * 保存分片信息
     * 
     * @param chunkInfo 分片信息
     */
    void saveChunk(ChunkInfo chunkInfo);
    
    /**
     * 根据上传ID和分片索引查找分片
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @return 分片信息
     */
    Optional<ChunkInfo> findChunk(String uploadId, Integer chunkIndex);
    
    /**
     * 获取上传ID的所有分片
     * 
     * @param uploadId 上传ID
     * @return 分片列表
     */
    List<ChunkInfo> findAllChunks(String uploadId);
    
    /**
     * 删除分片信息
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     */
    void deleteChunk(String uploadId, Integer chunkIndex);
    
    /**
     * 删除上传ID的所有分片
     * 
     * @param uploadId 上传ID
     */
    void deleteAllChunks(String uploadId);
    
    /**
     * 保存上传会话
     * 
     * @param session 上传会话
     */
    void saveSession(UploadSession session);
    
    /**
     * 根据上传ID查找上传会话
     * 
     * @param uploadId 上传ID
     * @return 上传会话
     */
    Optional<UploadSession> findSession(String uploadId);
    
    /**
     * 删除上传会话
     * 
     * @param uploadId 上传ID
     */
    void deleteSession(String uploadId);
}

