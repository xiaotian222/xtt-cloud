package xtt.cloud.oa.file.infrastructure;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.file.domain.ChunkInfo;
import xtt.cloud.oa.file.domain.ChunkStorage;
import xtt.cloud.oa.file.domain.UploadSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存分片存储实现（临时实现，后续可替换为数据库存储）
 * 
 * @author xtt
 */
@Component
public class InMemoryChunkStorage implements ChunkStorage {
    
    private final Map<String, ChunkInfo> chunkStorage = new ConcurrentHashMap<>();
    private final Map<String, UploadSession> sessionStorage = new ConcurrentHashMap<>();
    
    @Override
    public void saveChunk(ChunkInfo chunkInfo) {
        String key = chunkInfo.getUploadId() + "_" + chunkInfo.getChunkIndex();
        chunkStorage.put(key, chunkInfo);
    }
    
    @Override
    public Optional<ChunkInfo> findChunk(String uploadId, Integer chunkIndex) {
        String key = uploadId + "_" + chunkIndex;
        return Optional.ofNullable(chunkStorage.get(key));
    }
    
    @Override
    public List<ChunkInfo> findAllChunks(String uploadId) {
        return chunkStorage.values().stream()
                .filter(chunk -> chunk.getUploadId().equals(uploadId))
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteChunk(String uploadId, Integer chunkIndex) {
        String key = uploadId + "_" + chunkIndex;
        chunkStorage.remove(key);
    }
    
    @Override
    public void deleteAllChunks(String uploadId) {
        chunkStorage.entrySet().removeIf(entry -> entry.getValue().getUploadId().equals(uploadId));
    }
    
    @Override
    public void saveSession(UploadSession session) {
        sessionStorage.put(session.getUploadId(), session);
    }
    
    @Override
    public Optional<UploadSession> findSession(String uploadId) {
        return Optional.ofNullable(sessionStorage.get(uploadId));
    }
    
    @Override
    public void deleteSession(String uploadId) {
        sessionStorage.remove(uploadId);
    }
}

