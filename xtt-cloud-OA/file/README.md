# File Service - 文件服务

## 概述

文件服务提供文件上传、下载、预览和删除功能。

## 功能特性

- ✅ 文件上传
- ✅ 文件下载
- ✅ 文件预览（在线查看）
- ✅ 文件删除
- ✅ 文件信息查询
- ✅ 用户文件列表查询
- ✅ **大文件分片上传**
- ✅ **断点续传**
- ✅ **上传进度查询**

## API 接口

### 1. 上传文件

```http
POST /api/file/upload
Content-Type: multipart/form-data

参数:
- file: 文件（必填）
- userId: 用户ID（可选，可从 JWT token 中获取）
```

**响应示例:**
```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "test.pdf",
  "filename": "550e8400-e29b-41d4-a716-446655440000.pdf",
  "storagePath": "2024/12/26/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileSize": 102400,
  "contentType": "application/pdf",
  "extension": ".pdf",
  "uploadUserId": 1,
  "uploadTime": "2024-12-26T10:30:00"
}
```

### 2. 下载文件

```http
GET /api/file/download/{fileId}
```

**响应:**
- Content-Type: 根据文件类型自动设置
- Content-Disposition: attachment; filename="原始文件名"

### 3. 预览文件

```http
GET /api/file/preview/{fileId}
```

**响应:**
- Content-Type: 根据文件类型自动设置
- Content-Disposition: inline; filename="原始文件名"

### 4. 删除文件

```http
DELETE /api/file/{fileId}
```

**响应:**
- 204 No Content（成功）

### 5. 获取文件信息

```http
GET /api/file/{fileId}
```

**响应示例:**
```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "test.pdf",
  "filename": "550e8400-e29b-41d4-a716-446655440000.pdf",
  "storagePath": "2024/12/26/550e8400-e29b-41d4-a716-446655440000.pdf",
  "fileSize": 102400,
  "contentType": "application/pdf",
  "extension": ".pdf",
  "uploadUserId": 1,
  "uploadTime": "2024-12-26T10:30:00"
}
```

### 6. 获取用户文件列表

```http
GET /api/file/user/{userId}
```

**响应示例:**
```json
[
  {
    "fileId": "550e8400-e29b-41d4-a716-446655440000",
    "originalFilename": "test.pdf",
    "fileSize": 102400,
    "uploadTime": "2024-12-26T10:30:00"
  }
]
```

### 7. 初始化分片上传

```http
POST /api/file/chunk/init

参数:
- filename: 文件名（必填）
- totalSize: 文件总大小（字节，必填）
- chunkSize: 分片大小（字节，可选，默认 5MB）
- userId: 用户ID（可选）
```

**响应示例:**
```json
{
  "uploadId": "upload-123456",
  "originalFilename": "large-file.zip",
  "totalSize": 104857600,
  "chunkSize": 5242880,
  "totalChunks": 20,
  "extension": ".zip",
  "uploadUserId": 1,
  "createTime": "2024-12-26T10:30:00",
  "uploadedChunks": [],
  "completed": false
}
```

### 8. 上传分片

```http
POST /api/file/chunk/upload

参数:
- uploadId: 上传ID（必填）
- chunkIndex: 分片索引（从 0 开始，必填）
- chunk: 分片文件（必填）
```

**响应示例:**
```json
{
  "success": true,
  "uploadId": "upload-123456",
  "chunkIndex": 0
}
```

### 9. 合并分片

```http
POST /api/file/chunk/merge

参数:
- uploadId: 上传ID（必填）
```

**响应示例:**
```json
{
  "fileId": "550e8400-e29b-41d4-a716-446655440000",
  "originalFilename": "large-file.zip",
  "fileSize": 104857600,
  "uploadTime": "2024-12-26T10:30:00"
}
```

### 10. 获取上传进度

```http
GET /api/file/chunk/progress/{uploadId}
```

**响应示例:**
```json
{
  "uploadId": "upload-123456",
  "totalChunks": 20,
  "uploadedChunks": 15,
  "progress": 75.0,
  "uploadedChunkIndexes": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14],
  "completed": false
}
```

### 11. 取消上传

```http
DELETE /api/file/chunk/{uploadId}
```

**响应:**
- 204 No Content（成功）

## 配置说明

### application.yaml

```yaml
server:
  port: 8090

file:
  upload:
    # 文件存储根目录
    root-path: /app/files
    # 单个文件最大大小（MB）
    max-file-size: 100
    # 请求最大大小（MB）
    max-request-size: 200
    # 允许的文件类型（为空则允许所有类型）
    allowed-types:
    # 禁止的文件类型
    forbidden-types:
      - .exe
      - .bat
      - .sh
      - .jar
      - .war
```

## 文件存储

### 存储路径规则

文件按照日期组织存储：
```
/app/files/
  └── YYYY/
      └── MM/
          └── DD/
              └── {fileId}.{extension}
```

例如：
```
/app/files/2024/12/26/550e8400-e29b-41d4-a716-446655440000.pdf
```

### 存储实现

当前使用内存存储（`InMemoryFileStorage`），文件信息存储在内存中。后续可以替换为数据库存储。

## 使用示例

### 使用 curl 上传文件

```bash
curl -X POST http://localhost:8090/api/file/upload \
  -F "file=@/path/to/file.pdf" \
  -F "userId=1"
```

### 使用 curl 下载文件

```bash
curl -X GET http://localhost:8090/api/file/download/{fileId} \
  -o downloaded_file.pdf
```

### 使用 curl 预览文件

```bash
curl -X GET http://localhost:8090/api/file/preview/{fileId}
```

### 使用 curl 删除文件

```bash
curl -X DELETE http://localhost:8090/api/file/{fileId}
```

### 使用 curl 分片上传大文件

```bash
# 1. 初始化上传
UPLOAD_ID=$(curl -X POST "http://localhost:8090/api/file/chunk/init" \
  -F "filename=large-file.zip" \
  -F "totalSize=104857600" \
  -F "chunkSize=5242880" \
  -F "userId=1" | jq -r '.uploadId')

# 2. 上传分片（示例：上传第一个分片）
curl -X POST "http://localhost:8090/api/file/chunk/upload" \
  -F "uploadId=$UPLOAD_ID" \
  -F "chunkIndex=0" \
  -F "chunk=@chunk0.bin"

# 3. 查询上传进度
curl -X GET "http://localhost:8090/api/file/chunk/progress/$UPLOAD_ID"

# 4. 合并分片
curl -X POST "http://localhost:8090/api/file/chunk/merge" \
  -F "uploadId=$UPLOAD_ID"
```

### JavaScript 分片上传示例

```javascript
// 初始化上传
async function initUpload(filename, totalSize) {
  const response = await fetch('/api/file/chunk/init', {
    method: 'POST',
    body: new URLSearchParams({
      filename,
      totalSize: totalSize.toString(),
      chunkSize: '5242880' // 5MB
    })
  });
  return await response.json();
}

// 上传分片
async function uploadChunk(uploadId, chunkIndex, chunkBlob) {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  formData.append('chunkIndex', chunkIndex);
  formData.append('chunk', chunkBlob);
  
  const response = await fetch('/api/file/chunk/upload', {
    method: 'POST',
    body: formData
  });
  return await response.json();
}

// 合并分片
async function mergeChunks(uploadId) {
  const formData = new FormData();
  formData.append('uploadId', uploadId);
  
  const response = await fetch('/api/file/chunk/merge', {
    method: 'POST',
    body: formData
  });
  return await response.json();
}

// 完整的分片上传流程
async function uploadLargeFile(file) {
  const CHUNK_SIZE = 5 * 1024 * 1024; // 5MB
  const totalChunks = Math.ceil(file.size / CHUNK_SIZE);
  
  // 1. 初始化
  const session = await initUpload(file.name, file.size);
  const uploadId = session.uploadId;
  
  // 2. 上传所有分片
  for (let i = 0; i < totalChunks; i++) {
    const start = i * CHUNK_SIZE;
    const end = Math.min(start + CHUNK_SIZE, file.size);
    const chunk = file.slice(start, end);
    
    await uploadChunk(uploadId, i, chunk);
    
    // 更新进度
    console.log(`上传进度: ${((i + 1) / totalChunks * 100).toFixed(2)}%`);
  }
  
  // 3. 合并分片
  const fileInfo = await mergeChunks(uploadId);
  console.log('上传完成，文件ID:', fileInfo.fileId);
  
  return fileInfo;
}
```

## 安全考虑

1. **文件类型限制**: 配置 `forbidden-types` 禁止危险文件类型
2. **文件大小限制**: 配置 `max-file-size` 限制单个文件大小
3. **用户权限**: 后续可添加用户权限验证，确保用户只能访问自己的文件
4. **JWT 认证**: 建议集成 JWT 认证，从 token 中获取用户ID

## 后续优化

- [ ] 集成数据库存储文件元信息
- [x] 支持文件分片上传（大文件）
- [x] 支持断点续传
- [ ] 支持文件版本管理
- [ ] 支持文件共享和权限控制
- [ ] 集成对象存储（OSS/S3）
- [ ] 支持文件压缩和转换
- [ ] 添加文件病毒扫描
- [ ] 分片上传支持 MD5 校验
- [ ] 分片上传支持并发上传

## 技术栈

- Spring Boot 3.x
- Spring Cloud (Nacos)
- Java 17

## 端口

- 服务端口: 8090

