package xtt.cloud.oa.file.interfaces.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xtt.cloud.oa.file.application.ChunkUploadService;
import xtt.cloud.oa.file.domain.FileInfo;
import xtt.cloud.oa.file.domain.UploadSession;

import java.io.IOException;
import java.util.Map;

/**
 * 分片上传控制器
 * 
 * @author xtt
 */
@RestController
@RequestMapping("/api/file/chunk")
public class ChunkUploadController {
    
    private static final Logger log = LoggerFactory.getLogger(ChunkUploadController.class);
    
    private final ChunkUploadService chunkUploadService;
    
    public ChunkUploadController(ChunkUploadService chunkUploadService) {
        this.chunkUploadService = chunkUploadService;
    }
    
    /**
     * 初始化分片上传
     * 
     * @param filename 文件名
     * @param totalSize 文件总大小
     * @param chunkSize 分片大小（可选）
     * @param userId 用户ID（可选）
     * @return 上传会话
     */
    @PostMapping("/init")
    public ResponseEntity<UploadSession> initUpload(
            @RequestParam("filename") String filename,
            @RequestParam("totalSize") Long totalSize,
            @RequestParam(value = "chunkSize", required = false) Long chunkSize,
            @RequestParam(value = "userId", required = false) Long userId) {
        try {
            // TODO: 从 JWT token 中获取用户ID
            if (userId == null) {
                userId = 1L; // 临时默认值
            }
            
            UploadSession session = chunkUploadService.initUpload(filename, totalSize, chunkSize, userId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("初始化分片上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 上传分片
     * 
     * @param uploadId 上传ID
     * @param chunkIndex 分片索引
     * @param chunk 分片文件
     * @return 操作结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") Integer chunkIndex,
            @RequestParam("chunk") MultipartFile chunk) {
        try {
            chunkUploadService.uploadChunk(uploadId, chunkIndex, chunk);
            
            Map<String, Object> result = Map.of(
                    "success", true,
                    "uploadId", uploadId,
                    "chunkIndex", chunkIndex
            );
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error("分片上传参数错误", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("分片上传状态错误", e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("分片上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 合并分片
     * 
     * @param uploadId 上传ID
     * @return 文件信息
     */
    @PostMapping("/merge")
    public ResponseEntity<FileInfo> mergeChunks(@RequestParam("uploadId") String uploadId) {
        try {
            FileInfo fileInfo = chunkUploadService.mergeChunks(uploadId);
            return ResponseEntity.ok(fileInfo);
        } catch (IllegalArgumentException e) {
            log.error("合并分片参数错误", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            log.error("合并分片状态错误", e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("合并分片失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取上传进度
     * 
     * @param uploadId 上传ID
     * @return 上传进度
     */
    @GetMapping("/progress/{uploadId}")
    public ResponseEntity<Map<String, Object>> getProgress(@PathVariable String uploadId) {
        try {
            Map<String, Object> progress = chunkUploadService.getUploadProgress(uploadId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            log.error("获取上传进度失败", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 取消上传
     * 
     * @param uploadId 上传ID
     * @return 操作结果
     */
    @DeleteMapping("/{uploadId}")
    public ResponseEntity<Void> cancelUpload(@PathVariable String uploadId) {
        try {
            chunkUploadService.cancelUpload(uploadId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("取消上传失败", e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("取消上传失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

