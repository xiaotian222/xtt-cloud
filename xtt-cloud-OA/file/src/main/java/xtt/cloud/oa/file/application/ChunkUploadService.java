package xtt.cloud.oa.file.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xtt.cloud.oa.file.domain.ChunkInfo;
import xtt.cloud.oa.file.domain.ChunkStorage;
import xtt.cloud.oa.file.domain.FileInfo;
import xtt.cloud.oa.file.domain.FileStorage;
import xtt.cloud.oa.file.domain.UploadSession;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 分片上传服务
 * 
 * @author xtt
 */
@Service
public class ChunkUploadService {
    
    private static final Logger log = LoggerFactory.getLogger(ChunkUploadService.class);
    
    private final ChunkStorage chunkStorage;
    private final FileStorage fileStorage;
    private final String rootPath;
    
    @Value("${file.upload.chunk-size:5242880}") // 默认 5MB
    private long defaultChunkSize;
    
    public ChunkUploadService(
            ChunkStorage chunkStorage,
            FileStorage fileStorage,
            @Value("${file.upload.root-path:/app/files}") String rootPath) {
        this.chunkStorage = chunkStorage;
        this.fileStorage = fileStorage;
        this.rootPath = rootPath;
    }
    
    /**
     * 初始化分片上传
     * 
     * @param originalFilename 原始文件名
     * @param totalSize 文件总大小
     * @param chunkSize 分片大小
     * @param userId 用户ID
     * @return 上传会话
     */
    public UploadSession initUpload(String originalFilename, Long totalSize, Long chunkSize, Long userId) {
        log.info("初始化分片上传，文件名: {}, 总大小: {}, 分片大小: {}, 用户ID: {}", 
                originalFilename, totalSize, chunkSize, userId);
        
        if (chunkSize == null || chunkSize <= 0) {
            chunkSize = defaultChunkSize;
        }
        
        // 计算总分片数
        int totalChunks = (int) Math.ceil((double) totalSize / chunkSize);
        
        // 生成上传ID
        String uploadId = UUID.randomUUID().toString();
        
        // 获取文件扩展名
        String extension = getFileExtension(originalFilename);
        
        // 创建上传会话
        UploadSession session = UploadSession.builder()
                .uploadId(uploadId)
                .originalFilename(originalFilename)
                .totalSize(totalSize)
                .chunkSize(chunkSize)
                .totalChunks(totalChunks)
                .extension(extension)
                .uploadUserId(userId)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .uploadedChunks(new ArrayList<>())
                .completed(false)
                .build();
        
        chunkStorage.saveSession(session);
        
        log.info("分片上传初始化成功，上传ID: {}, 总分片数: {}", uploadId, totalChunks);
        return session;
    }
    
    /**
     * 上传分片
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @param chunkFile 分片文件
     * @return 分片信息
     */
    public ChunkInfo uploadChunk(String uploadId, Integer chunkIndex, MultipartFile chunkFile) throws IOException {
        log.debug("上传分片，上传ID: {}, 分片索引: {}, 分片大小: {}", 
                uploadId, chunkIndex, chunkFile.getSize());
        
        // 验证上传会话
        UploadSession session = chunkStorage.findSession(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在: " + uploadId));
        
        if (session.getCompleted()) {
            throw new IllegalStateException("文件已合并完成，不能继续上传分片");
        }
        
        if (chunkIndex < 0 || chunkIndex >= session.getTotalChunks()) {
            throw new IllegalArgumentException("分片索引超出范围: " + chunkIndex);
        }
        
        // 检查分片是否已上传
        if (chunkStorage.findChunk(uploadId, chunkIndex).isPresent()) {
            log.debug("分片已存在，跳过上传，上传ID: {}, 分片索引: {}", uploadId, chunkIndex);
            return chunkStorage.findChunk(uploadId, chunkIndex).get();
        }
        
        // 生成分片存储路径
        String chunkPath = generateChunkPath(uploadId, chunkIndex);
        Path chunkFilePath = Paths.get(rootPath, "chunks", chunkPath);
        Files.createDirectories(chunkFilePath.getParent());
        
        // 保存分片文件
        chunkFile.transferTo(chunkFilePath.toFile());
        
        // 创建分片信息
        ChunkInfo chunkInfo = ChunkInfo.builder()
                .uploadId(uploadId)
                .chunkIndex(chunkIndex)
                .chunkSize(chunkFile.getSize())
                .chunkPath(chunkPath)
                .uploadTime(LocalDateTime.now())
                .build();
        
        chunkStorage.saveChunk(chunkInfo);
        
        // 更新上传会话
        List<Integer> uploadedChunks = new ArrayList<>(session.getUploadedChunks());
        if (!uploadedChunks.contains(chunkIndex)) {
            uploadedChunks.add(chunkIndex);
            Collections.sort(uploadedChunks);
        }
        session.setUploadedChunks(uploadedChunks);
        session.setUpdateTime(LocalDateTime.now());
        chunkStorage.saveSession(session);
        
        log.debug("分片上传成功，上传ID: {}, 分片索引: {}", uploadId, chunkIndex);
        return chunkInfo;
    }
    
    /**
     * 合并分片
     * 
     * @param uploadId 上传ID
     * @return 文件信息
     */
    public FileInfo mergeChunks(String uploadId) throws IOException {
        log.info("合并分片，上传ID: {}", uploadId);
        
        // 获取上传会话
        UploadSession session = chunkStorage.findSession(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在: " + uploadId));
        
        if (session.getCompleted()) {
            // 如果已合并，直接返回文件信息
            return fileStorage.findByFileId(session.getFileId())
                    .orElseThrow(() -> new IllegalStateException("文件信息不存在: " + session.getFileId()));
        }
        
        // 获取所有分片
        List<ChunkInfo> chunks = chunkStorage.findAllChunks(uploadId);
        
        // 验证分片完整性
        validateChunks(session, chunks);
        
        // 生成文件ID和存储路径
        String fileId = UUID.randomUUID().toString();
        String storagePath = generateStoragePath(fileId, session.getExtension());
        Path finalFilePath = Paths.get(rootPath, storagePath);
        Files.createDirectories(finalFilePath.getParent());
        
        // 合并分片
        try (FileOutputStream fos = new FileOutputStream(finalFilePath.toFile());
             FileChannel outChannel = fos.getChannel()) {
            
            // 按索引排序
            chunks.sort(Comparator.comparing(ChunkInfo::getChunkIndex));
            
            // 逐个合并分片
            for (ChunkInfo chunk : chunks) {
                Path chunkPath = Paths.get(rootPath, "chunks", chunk.getChunkPath());
                try (FileInputStream fis = new FileInputStream(chunkPath.toFile());
                     FileChannel inChannel = fis.getChannel()) {
                    
                    long position = chunk.getChunkIndex() * session.getChunkSize();
                    long transferred = 0;
                    long size = chunk.getChunkSize();
                    
                    while (transferred < size) {
                        transferred += inChannel.transferTo(0, size - transferred, outChannel);
                    }
                }
            }
        }
        
        // 创建文件信息
        FileInfo fileInfo = FileInfo.builder()
                .fileId(fileId)
                .originalFilename(session.getOriginalFilename())
                .filename(finalFilePath.getFileName().toString())
                .storagePath(storagePath)
                .fileSize(session.getTotalSize())
                .contentType(session.getContentType())
                .extension(session.getExtension())
                .uploadUserId(session.getUploadUserId())
                .uploadTime(LocalDateTime.now())
                .build();
        
        fileStorage.save(fileInfo);
        
        // 更新上传会话
        session.setCompleted(true);
        session.setFileId(fileId);
        session.setUpdateTime(LocalDateTime.now());
        chunkStorage.saveSession(session);
        
        // 清理分片文件（可选，可以保留一段时间用于恢复）
        // cleanupChunks(uploadId);
        
        log.info("分片合并成功，上传ID: {}, 文件ID: {}", uploadId, fileId);
        return fileInfo;
    }
    
    /**
     * 获取上传进度
     * 
     * @param uploadId 上传ID
     * @return 上传进度信息
     */
    public Map<String, Object> getUploadProgress(String uploadId) {
        UploadSession session = chunkStorage.findSession(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("上传会话不存在: " + uploadId));
        
        List<Integer> uploadedChunks = session.getUploadedChunks();
        int uploadedCount = uploadedChunks.size();
        int totalChunks = session.getTotalChunks();
        double progress = totalChunks > 0 ? (double) uploadedCount / totalChunks * 100 : 0;
        
        Map<String, Object> progressInfo = new HashMap<>();
        progressInfo.put("uploadId", uploadId);
        progressInfo.put("totalChunks", totalChunks);
        progressInfo.put("uploadedChunks", uploadedCount);
        progressInfo.put("progress", Math.round(progress * 100.0) / 100.0);
        progressInfo.put("uploadedChunkIndexes", uploadedChunks);
        progressInfo.put("completed", session.getCompleted());
        
        return progressInfo;
    }
    
    /**
     * 取消上传（删除分片和会话）
     * 
     * @param uploadId 上传ID
     */
    public void cancelUpload(String uploadId) throws IOException {
        log.info("取消上传，上传ID: {}", uploadId);
        
        // 删除所有分片文件
        List<ChunkInfo> chunks = chunkStorage.findAllChunks(uploadId);
        for (ChunkInfo chunk : chunks) {
            Path chunkPath = Paths.get(rootPath, "chunks", chunk.getChunkPath());
            if (Files.exists(chunkPath)) {
                Files.delete(chunkPath);
            }
        }
        
        // 删除分片信息和会话
        chunkStorage.deleteAllChunks(uploadId);
        chunkStorage.deleteSession(uploadId);
        
        log.info("上传已取消，上传ID: {}", uploadId);
    }
    
    /**
     * 验证分片完整性
     */
    private void validateChunks(UploadSession session, List<ChunkInfo> chunks) {
        if (chunks.size() != session.getTotalChunks()) {
            throw new IllegalStateException(
                    String.format("分片数量不完整，期望: %d, 实际: %d", 
                            session.getTotalChunks(), chunks.size()));
        }
        
        // 检查分片索引是否连续
        Set<Integer> chunkIndexes = chunks.stream()
                .map(ChunkInfo::getChunkIndex)
                .collect(Collectors.toSet());
        
        Set<Integer> expectedIndexes = IntStream.range(0, session.getTotalChunks())
                .boxed()
                .collect(Collectors.toSet());
        
        if (!chunkIndexes.equals(expectedIndexes)) {
            throw new IllegalStateException("分片索引不连续");
        }
    }
    
    /**
     * 生成分片存储路径
     */
    private String generateChunkPath(String uploadId, Integer chunkIndex) {
        return String.format("%s/%d", uploadId, chunkIndex);
    }
    
    /**
     * 生成文件存储路径
     */
    private String generateStoragePath(String fileId, String extension) {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%d/%02d/%02d/%s%s",
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth(),
                fileId,
                extension);
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
    
    /**
     * 清理分片文件
     */
    private void cleanupChunks(String uploadId) throws IOException {
        List<ChunkInfo> chunks = chunkStorage.findAllChunks(uploadId);
        for (ChunkInfo chunk : chunks) {
            Path chunkPath = Paths.get(rootPath, "chunks", chunk.getChunkPath());
            if (Files.exists(chunkPath)) {
                Files.delete(chunkPath);
            }
        }
        chunkStorage.deleteAllChunks(uploadId);
    }
}

