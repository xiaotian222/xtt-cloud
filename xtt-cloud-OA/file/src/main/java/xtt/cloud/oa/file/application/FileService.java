package xtt.cloud.oa.file.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xtt.cloud.oa.file.domain.FileInfo;
import xtt.cloud.oa.file.domain.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文件服务
 * 
 * @author xtt
 */
@Service
public class FileService {
    
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    
    private final FileStorage fileStorage;
    private final String rootPath;
    
    public FileService(
            FileStorage fileStorage,
            @Value("${file.upload.root-path:/app/files}") String rootPath) {
        this.fileStorage = fileStorage;
        this.rootPath = rootPath;
    }
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param userId 用户ID
     * @return 文件信息
     */
    public FileInfo uploadFile(MultipartFile file, Long userId) throws IOException {
        log.info("上传文件，文件名: {}, 用户ID: {}", file.getOriginalFilename(), userId);
        
        // 验证文件
        validateFile(file);
        
        // 生成文件ID和存储路径
        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storagePath = generateStoragePath(fileId, extension);
        
        // 保存文件
        Path filePath = Paths.get(rootPath, storagePath);
        Files.createDirectories(filePath.getParent());
        file.transferTo(filePath.toFile());
        
        // 创建文件信息
        FileInfo fileInfo = FileInfo.builder()
                .fileId(fileId)
                .originalFilename(originalFilename)
                .filename(filePath.getFileName().toString())
                .storagePath(storagePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .extension(extension)
                .uploadUserId(userId)
                .uploadTime(LocalDateTime.now())
                .build();
        
        // 保存文件信息到数据库
        fileStorage.save(fileInfo);
        
        log.info("文件上传成功，文件ID: {}, 存储路径: {}", fileId, storagePath);
        return fileInfo;
    }
    
    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    public FileInfo downloadFile(String fileId) {
        log.info("下载文件，文件ID: {}", fileId);
        
        FileInfo fileInfo = fileStorage.findByFileId(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileId));
        
        Path filePath = Paths.get(rootPath, fileInfo.getStoragePath());
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("文件不存在于存储路径: " + filePath);
        }
        
        return fileInfo;
    }
    
    /**
     * 获取文件路径
     * 
     * @param fileId 文件ID
     * @return 文件路径
     */
    public Path getFilePath(String fileId) {
        FileInfo fileInfo = fileStorage.findByFileId(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileId));
        
        return Paths.get(rootPath, fileInfo.getStoragePath());
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     */
    public void deleteFile(String fileId) throws IOException {
        log.info("删除文件，文件ID: {}", fileId);
        
        FileInfo fileInfo = fileStorage.findByFileId(fileId)
                .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + fileId));
        
        // 删除物理文件
        Path filePath = Paths.get(rootPath, fileInfo.getStoragePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // 删除数据库记录
        fileStorage.delete(fileId);
        
        log.info("文件删除成功，文件ID: {}", fileId);
    }
    
    /**
     * 获取用户上传的文件列表
     * 
     * @param userId 用户ID
     * @return 文件列表
     */
    public List<FileInfo> getUserFiles(Long userId) {
        return fileStorage.findByUploadUserId(userId);
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        // TODO: 可以添加文件类型、大小等验证
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
     * 生成存储路径
     * 格式: YYYY/MM/DD/fileId.extension
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
}

