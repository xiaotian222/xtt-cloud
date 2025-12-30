package xtt.cloud.oa.file.interfaces.rest;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xtt.cloud.oa.file.application.FileService;
import xtt.cloud.oa.file.domain.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 文件控制器
 * 
 * @author xtt
 */
@RestController
@RequestMapping("/api/file")
public class FileController {
    
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    
    private final FileService fileService;
    private final String rootPath;
    
    public FileController(
            FileService fileService,
            @Value("${file.upload.root-path:/app/files}") String rootPath) {
        this.fileService = fileService;
        this.rootPath = rootPath;
    }
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param userId 用户ID（可以从 JWT token 中获取）
     * @return 文件信息
     */
    @PostMapping("/upload")
    public ResponseEntity<FileInfo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            // TODO: 从 JWT token 中获取用户ID
            if (userId == null) {
                userId = 1L; // 临时默认值
            }
            
            FileInfo fileInfo = fileService.uploadFile(file, userId);
            return ResponseEntity.ok(fileInfo);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            log.error("文件上传参数错误", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @return 文件
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        try {
            FileInfo fileInfo = fileService.downloadFile(fileId);
            Path filePath = fileService.getFilePath(fileId);
            
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(
                            fileInfo.getContentType() != null ? fileInfo.getContentType() : "application/octet-stream"))
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("文件下载失败，文件ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 预览文件（在线查看）
     * 
     * @param fileId 文件ID
     * @return 文件
     */
    @GetMapping("/preview/{fileId}")
    public ResponseEntity<Resource> previewFile(@PathVariable String fileId) {
        try {
            FileInfo fileInfo = fileService.downloadFile(fileId);
            Path filePath = fileService.getFilePath(fileId);
            
            Resource resource = new FileSystemResource(filePath.toFile());
            
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // 设置 Content-Type
            String contentType = fileInfo.getContentType();
            if (contentType == null) {
                try {
                    contentType = Files.probeContentType(filePath);
                } catch (IOException e) {
                    contentType = "application/octet-stream";
                }
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "inline; filename=\"" + fileInfo.getOriginalFilename() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (IllegalArgumentException e) {
            log.error("文件预览失败，文件ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("文件预览失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 删除文件
     * 
     * @param fileId 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileId) {
        try {
            fileService.deleteFile(fileId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("文件删除失败，文件ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取文件信息
     * 
     * @param fileId 文件ID
     * @return 文件信息
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<FileInfo> getFileInfo(@PathVariable String fileId) {
        try {
            FileInfo fileInfo = fileService.downloadFile(fileId);
            return ResponseEntity.ok(fileInfo);
        } catch (IllegalArgumentException e) {
            log.error("获取文件信息失败，文件ID: {}", fileId, e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取用户文件列表
     * 
     * @param userId 用户ID
     * @return 文件列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FileInfo>> getUserFiles(@PathVariable Long userId) {
        List<FileInfo> files = fileService.getUserFiles(userId);
        return ResponseEntity.ok(files);
    }
}

