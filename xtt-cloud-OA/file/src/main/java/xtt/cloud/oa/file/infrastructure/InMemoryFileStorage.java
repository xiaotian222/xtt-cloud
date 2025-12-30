package xtt.cloud.oa.file.infrastructure;

import org.springframework.stereotype.Component;
import xtt.cloud.oa.file.domain.FileInfo;
import xtt.cloud.oa.file.domain.FileStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存文件存储实现（临时实现，后续可替换为数据库存储）
 * 
 * @author xtt
 */
@Component
public class InMemoryFileStorage implements FileStorage {
    
    private final Map<String, FileInfo> storage = new ConcurrentHashMap<>();
    
    @Override
    public void save(FileInfo fileInfo) {
        storage.put(fileInfo.getFileId(), fileInfo);
    }
    
    @Override
    public Optional<FileInfo> findByFileId(String fileId) {
        return Optional.ofNullable(storage.get(fileId));
    }
    
    @Override
    public List<FileInfo> findByUploadUserId(Long userId) {
        List<FileInfo> result = new ArrayList<>();
        for (FileInfo fileInfo : storage.values()) {
            if (fileInfo.getUploadUserId() != null && fileInfo.getUploadUserId().equals(userId)) {
                result.add(fileInfo);
            }
        }
        return result;
    }
    
    @Override
    public void delete(String fileId) {
        storage.remove(fileId);
    }
}

