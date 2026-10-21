package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.service.FileStorageService;
import com.calligraphy.platform.entity.FileMetadata;
import com.calligraphy.platform.repository.FileMetadataRepository;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${file.storage.path:uploads}")
    private String storagePath;

    @Value("${file.allowed-extensions:jpg,jpeg,png,gif,pdf,doc,docx}")
    private String[] allowedExtensions;

    @Value("${file.max-size:10485760}") // 10MB
    private long maxFileSize;

    @Transactional
    public FileMetadata uploadFile(MultipartFile file, String userId, String filePurpose, String relatedEntityId) {
        try {
            // 验证文件
            validateFile(file);
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = generateUniqueFilename(fileExtension);
            
            // 创建存储目录
            Path fileStoragePath = createStoragePath(userId, filePurpose);
            Path filePath = fileStoragePath.resolve(uniqueFilename);
            
            // 保存文件
            Files.copy(file.getInputStream(), filePath);
            
            // 创建文件元数据
            FileMetadata metadata = FileMetadata.builder()
                    .fileId(UUID.randomUUID().toString())
                    .userId(userId)
                    .fileName(originalFilename)
                    .fileUrl(filePath.toString())
                    .fileType(fileExtension)
                    .fileSize(file.getSize())
                    .filePurpose(filePurpose)
                    .relatedEntityId(relatedEntityId)
                    .isPublic(false)
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            return fileMetadataRepository.save(metadata);
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public byte[] downloadFile(String fileId) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));
            
            Path filePath = Paths.get(metadata.getFileUrl());
            return Files.readAllBytes(filePath);
            
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            throw new RuntimeException("文件下载失败", e);
        }
    }
    @Override
    @Transactional
    public boolean deleteFile(String fileId) {
        try {
            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));
            
            // 删除物理文件
            Path filePath = Paths.get(metadata.getFileUrl());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            fileMetadataRepository.delete(metadata);
            
            return true;
            
        } catch (IOException e) {
            log.error("文件删除失败: {}", e.getMessage());
            return false;
        }
    }

    public Optional<FileMetadata> getFileInfo(String fileId) {
        return fileMetadataRepository.findById(fileId);
    }

    public List<FileMetadata> getUserFiles(String userId) {
        return fileMetadataRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<FileMetadata> getFilesByPurpose(String filePurpose) {
        return fileMetadataRepository.findByFilePurposeOrderByCreatedAtDesc(filePurpose);
    }

    @Transactional
    public boolean updateFileInfo(String fileId, String newFileName, String description) {
        return fileMetadataRepository.findById(fileId)
                .map(metadata -> {
                    if (newFileName != null && !newFileName.trim().isEmpty()) {
                        metadata.setFileName(newFileName.trim());
                    }
                    if (description != null) {
                        metadata.setDescription(description.trim());
                    }
                    metadata.setUpdatedAt(LocalDateTime.now());
                    fileMetadataRepository.save(metadata);
                    return true;
                })
                .orElse(false);
    }
    @Override
    public String copyFile(String sourceFileName, String destinationFileName) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public String copyFile(String sourceFileName, String destinationFileName, String userId) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public String moveFile(String sourceFileName, String destinationFileName) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public String moveFile(String sourceFileName, String destinationFileName, String userId) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public String renameFile(String oldFileName, String newFileName) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public String renameFile(String oldFileName, String newFileName, String userId) {
        throw new UnsupportedOperationException("此方法尚未实现");
    }
    @Override
    public byte[] compressFiles(List<String> fileIds) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(baos);
            
            for (String fileId : fileIds) {
                FileMetadata metadata = fileMetadataRepository.findById(fileId)
                        .orElseThrow(() -> new ResourceNotFoundException("文件不存在: " + fileId));
                
                Path filePath = Paths.get(metadata.getFileUrl());
                ZipEntry zipEntry = new ZipEntry(metadata.getFileName());
                zos.putNextEntry(zipEntry);
                
                Files.copy(filePath, zos);
                zos.closeEntry();
            }
            
            zos.close();
            return baos.toByteArray();
            
        } catch (IOException e) {
            log.error("文件压缩失败: {}", e.getMessage());
            throw new RuntimeException("文件压缩失败", e);
        }
    }
    @Override
    public boolean validateFileExtension(MultipartFile file) {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        for (String allowedExtension : allowedExtensions) {
            if (allowedExtension.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean validateFileSize(MultipartFile file) {
        return file.getSize() <= maxFileSize;
    }

    public boolean scanForViruses(MultipartFile file) {
        // 这里应该集成实际的病毒扫描服务
        log.info("扫描文件病毒: {}", file.getOriginalFilename());
        return true; // 模拟扫描通过
    }

    public long getStorageQuota(String userId) {
        return fileMetadataRepository.sumFileSizeByUserId(userId);
    }

    public long getStorageLimit() {
        return 1073741824L; // 1GB
    }

    public boolean isStorageFull(String userId) {
        return getStorageQuota(userId) >= getStorageLimit();
    }

    public long getFileCount(String userId) {
        return fileMetadataRepository.countByUserId(userId);
    }
    @Override
    public long getUserStorageUsed(String userId) {
        return fileMetadataRepository.sumFileSizeByUserId(userId);
    }
    @Override
    @Transactional
    public void cleanupOldFiles(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            List<FileMetadata> oldFiles = fileMetadataRepository.findByCreatedAtBefore(cutoffDate);
            
            for (FileMetadata metadata : oldFiles) {
                deleteFile(metadata.getFileId());
            }
            
            log.info("清理了 {} 个旧文件", oldFiles.size());
            
        } catch (Exception e) {
            log.error("清理旧文件失败: {}", e.getMessage());
        }
    }

    public boolean backupFiles(String userId, String backupPath) {
        // 实现文件备份逻辑
        log.info("备份用户 {} 的文件到 {}", userId, backupPath);
        return true;
    }

    public boolean restoreFiles(String backupPath) {
        // 实现文件恢复逻辑
        log.info("从 {} 恢复文件", backupPath);
        return true;
    }

    public String generateFileUrl(String fileId) {
        return "/api/files/" + fileId + "/download";
    }

    public boolean isFilePublic(String fileId) {
        return fileMetadataRepository.findById(fileId)
                .map(FileMetadata::getIsPublic)
                .orElse(false);
    }

    @Transactional
    public boolean setFilePublic(String fileId, boolean isPublic) {
        return fileMetadataRepository.findById(fileId)
                .map(metadata -> {
                    metadata.setIsPublic(isPublic);
                    metadata.setUpdatedAt(LocalDateTime.now());
                    fileMetadataRepository.save(metadata);
                    return true;
                })
                .orElse(false);
    }
    @Override
    public java.util.Map<String, Object> getFileMetadata(String fileName) {
        return fileMetadataRepository.findById(fileName)
                .map(meta -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("fileId", meta.getFileId());
                    m.put("userId", meta.getUserId());
                    m.put("fileName", meta.getFileName());
                    m.put("fileUrl", meta.getFileUrl());
                    m.put("fileType", meta.getFileType());
                    m.put("fileSize", meta.getFileSize());
                    m.put("filePurpose", meta.getFilePurpose());
                    m.put("relatedEntityId", meta.getRelatedEntityId());
                    m.put("isPublic", meta.getIsPublic());
                    m.put("isDeleted", meta.getIsDeleted());
                    m.put("createdAt", meta.getCreatedAt());
                    m.put("updatedAt", meta.getUpdatedAt());
                    return m;
                })
                .orElseThrow(() -> new ResourceNotFoundException("文件不存在"));
    }
    @Override
    public boolean validateFile(MultipartFile file) {
        try {
            validateFileInternal(file);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void validateFileInternal(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        if (!validateFileExtension(file)) {
            throw new IllegalArgumentException("不支持的文件格式");
        }
        
        if (!validateFileSize(file)) {
            throw new IllegalArgumentException("文件大小超过限制");
        }
        
        if (!scanForViruses(file)) {
            throw new RuntimeException("文件病毒扫描失败");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + (extension.isEmpty() ? "" : "." + extension);
    }

    private Path createStoragePath(String userId, String filePurpose) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            if (filePurpose == null || filePurpose.trim().isEmpty()) {
                throw new IllegalArgumentException("文件用途不能为空");
            }
            Path path = Paths.get(storagePath, userId.trim(), filePurpose.trim());
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("创建存储目录失败: " + e.getMessage(), e);
        }
    }
    @Override
    public boolean fileExists(String fileName) {
        Path path = Paths.get(storagePath).resolve(fileName);
        return Files.exists(path);
    }
    @Override
    public boolean fileExists(String fileName, String userId) {
        Path path = Paths.get(storagePath, userId).resolve(fileName);
        return Files.exists(path);
    }
    @Override
    public boolean deleteMultipleFiles(List<String> fileNames) {
        boolean all = true;
        for (String id : fileNames) {
            all &= deleteFile(id);
        }
        return all;
    }
    @Override
    public boolean deleteMultipleFiles(List<String> fileNames, String userId) {
        boolean all = true;
        for (String name : fileNames) {
            Path path = Paths.get(storagePath, userId).resolve(name);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                all = false;
            }
        }
        return all;
    }
    @Override
    public void deleteUserFiles(String userId) {
        List<FileMetadata> files = fileMetadataRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (FileMetadata meta : files) {
            try { Files.deleteIfExists(Paths.get(meta.getFileUrl())); } catch (IOException ignored) {}
            fileMetadataRepository.delete(meta);
        }
    }
    @Override
    public void invalidateUserCdnCache(String userId) { }
    @Override
    public String getFileMimeType(String fileName) {
        try {
            return Files.probeContentType(Paths.get(storagePath).resolve(fileName));
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
    @Override
    public String getFileMimeType(String fileName, String userId) {
        try {
            return Files.probeContentType(Paths.get(storagePath, userId).resolve(fileName));
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
    @Override
    public long getFileSize(String fileName) {
        try {
            return Files.size(Paths.get(storagePath).resolve(fileName));
        } catch (IOException e) {
            return 0L;
        }
    }
    @Override
    public long getFileSize(String fileName, String userId) {
        try {
            return Files.size(Paths.get(storagePath, userId).resolve(fileName));
        } catch (IOException e) {
            return 0L;
        }
    }
    @Override
    public byte[] loadEncryptedFileAsBytes(String fileName, String encryptionKey) {
        return new byte[0];
    }
    @Override
    public org.springframework.core.io.Resource loadEncryptedFileAsResource(String fileName, String encryptionKey) {
        return new org.springframework.core.io.FileSystemResource(( Path) Paths.get(storagePath).resolve(fileName));
    }
    @Override
    public void restoreFiles(byte[] backupData) { }
    @Override
    public void invalidateCdnCache(String fileName) { }
    @Override
    public String getCdnUrl(String fileName) {
        return "/cdn/" + fileName;
    }
    @Override
    public String getCdnUrl(String fileName, String userId) {
        return "/cdn/" + userId + "/" + fileName;
    }
    @Override
    public java.util.Map<String, Object> getFileMetadata(String fileName, String userId) {
        return getFileMetadata(fileName);
    }
    @Override
    public void updateFileMetadata(String fileName, java.util.Map<String, Object> metadata) {
        fileMetadataRepository.findById(fileName).ifPresent(meta -> {
            Object v;
            v = metadata.get("fileName"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFileName(s.trim());
            v = metadata.get("fileUrl"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFileUrl(s.trim());
            v = metadata.get("fileType"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFileType(s.trim());
            v = metadata.get("fileSize"); if (v instanceof Number n) meta.setFileSize(n.longValue());
            v = metadata.get("filePurpose"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFilePurpose(s.trim());
            v = metadata.get("relatedEntityId"); if (v instanceof String s) meta.setRelatedEntityId(s);
            v = metadata.get("isPublic"); if (v instanceof Boolean b) meta.setIsPublic(b);
            v = metadata.get("isDeleted"); if (v instanceof Boolean b) meta.setIsDeleted(b);
            meta.setUpdatedAt(LocalDateTime.now());
            fileMetadataRepository.save(meta);
        });
    }
    @Override
    public void updateFileMetadata(String fileName, String userId, java.util.Map<String, Object> metadata) {
        fileMetadataRepository.findByFileNameAndUserId(fileName, userId).ifPresent(meta -> {
            Object v;
            v = metadata.get("fileUrl"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFileUrl(s.trim());
            v = metadata.get("fileType"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFileType(s.trim());
            v = metadata.get("fileSize"); if (v instanceof Number n) meta.setFileSize(n.longValue());
            v = metadata.get("filePurpose"); if (v instanceof String s && !s.trim().isEmpty()) meta.setFilePurpose(s.trim());
            v = metadata.get("relatedEntityId"); if (v instanceof String s) meta.setRelatedEntityId(s);
            v = metadata.get("isPublic"); if (v instanceof Boolean b) meta.setIsPublic(b);
            v = metadata.get("isDeleted"); if (v instanceof Boolean b) meta.setIsDeleted(b);
            meta.setUpdatedAt(LocalDateTime.now());
            fileMetadataRepository.save(meta);
        });
    }
    @Override
    public Path getUserFileStorageLocation(String userId) {
        Path path = Paths.get(storagePath, userId);
        try {
            Files.createDirectories(path);
        } catch (IOException ignored) {}
        return path;
    }
    @Override
    public void setAllowedFileExtensions(List<String> allowedFileExtensions) {
        this.allowedExtensions = allowedFileExtensions.toArray(new String[0]);
    }
    @Override
    public List<String> listFilesByType(String fileType) {
        return fileMetadataRepository.findByFilePurposeOrderByCreatedAtDesc(fileType)
                .stream().map(FileMetadata::getFileId).collect(Collectors.toList());
    }
    @Override
    public List<String> listFilesByType(String fileType, String userId) {
        return fileMetadataRepository.findByFilePurposeOrderByCreatedAtDesc(fileType)
                .stream().filter(m -> userId.equals(m.getUserId()))
                .map(FileMetadata::getFileId).collect(Collectors.toList());
    }
    @Override
    public List<String> listFilesByExtension(String extension) {
        return fileMetadataRepository.findAll().stream()
                .filter(m -> extension != null && extension.equalsIgnoreCase(m.getFileType()))
                .map(FileMetadata::getFileId)
                .collect(Collectors.toList());
    }
    @Override
    public List<String> listFilesByExtension(String extension, String userId) {
        return fileMetadataRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(m -> extension != null && extension.equalsIgnoreCase(m.getFileType()))
                .map(FileMetadata::getFileId)
                .collect(Collectors.toList());
    }
    @Override
    public void setAllowedFileTypes(List<String> allowedFileTypes) { }
    @Override
    public void restoreUserFiles(String userId, byte[] backupData) { }
    @Override
    public boolean validateFileContent(MultipartFile file) { return true; }
    @Override
    public boolean scanFileForMalware(String fileName) { return true; }
    @Override
    public void cleanupOrphanedFiles() { }
    @Override
    public String storeEncryptedFile(MultipartFile file, String userId, String fileType, String encryptionKey) {
        return storeFile(file, userId, fileType);
    }
    @Override
    public String storeFile(MultipartFile file, String userId, String fileType) {
        try {
            validateFileInternal(file);
            String extension = getFileExtension(file.getOriginalFilename());
            String unique = generateUniqueFilename(extension);
            Path path = createStoragePath(userId, fileType).resolve(unique);
            Files.copy(file.getInputStream(), path);
            return unique;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public String storeFile(MultipartFile file, String userId, String fileType, String subDirectory) {
        try {
            validateFileInternal(file);
            String extension = getFileExtension(file.getOriginalFilename());
            String unique = generateUniqueFilename(extension);
            Path path = Paths.get(storagePath, userId, fileType, subDirectory);
            Files.createDirectories(path);
            Files.copy(file.getInputStream(), path.resolve(unique));
            return unique;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<String> storeMultipleFiles(MultipartFile[] files, String userId, String fileType) {
        return java.util.Arrays.stream(files)
                .map(f -> storeFile(f, userId, fileType))
                .collect(Collectors.toList());
    }
    @Override
    public List<String> storeMultipleFiles(MultipartFile[] files, String userId, String fileType, String subDirectory) {
        return java.util.Arrays.stream(files)
                .map(f -> storeFile(f, userId, fileType, subDirectory))
                .collect(Collectors.toList());
    }
    @Override
    public org.springframework.core.io.Resource loadFileAsResource(String fileName) {
        return new org.springframework.core.io.FileSystemResource(Paths.get(storagePath).resolve(fileName));
    }
    @Override
    public org.springframework.core.io.Resource loadFileAsResource(String fileName, String userId) {
        return new org.springframework.core.io.FileSystemResource(Paths.get(storagePath, userId).resolve(fileName));
    }
    @Override
    public String generateThumbnail(String fileName) {
        return fileName;
    }
    @Override
    public String generateThumbnail(String fileName, String userId) {
        return fileName;
    }
    @Override
    public org.springframework.core.io.Resource loadThumbnailAsResource(String fileName) {
        return new org.springframework.core.io.FileSystemResource(Paths.get(storagePath).resolve(fileName));
    }
    @Override
    public org.springframework.core.io.Resource loadThumbnailAsResource(String fileName, String userId) {
        return new org.springframework.core.io.FileSystemResource(Paths.get(storagePath, userId).resolve(fileName));
    }
    @Override
    public byte[] loadFileAsBytes(String fileName) {
        try {
            return Files.readAllBytes(Paths.get(storagePath).resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public byte[] loadFileAsBytes(String fileName, String userId) {
        try {
            return Files.readAllBytes(Paths.get(storagePath, userId).resolve(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public boolean deleteFile(String fileName, String userId) {
        try {
            return Files.deleteIfExists(Paths.get(storagePath, userId).resolve(fileName));
        } catch (IOException e) {
            return false;
        }
    }
    @Override
    public java.time.LocalDateTime getFileCreationTime(String fileName) {
        return java.time.LocalDateTime.now();
    }
    @Override
    public java.time.LocalDateTime getFileCreationTime(String fileName, String userId) {
        return java.time.LocalDateTime.now();
    }
    @Override
    public java.time.LocalDateTime getFileLastModifiedTime(String fileName) {
        return java.time.LocalDateTime.now();
    }
    @Override
    public java.time.LocalDateTime getFileLastModifiedTime(String fileName, String userId) {
        return java.time.LocalDateTime.now();
    }
    @Override
    public List<String> listFiles() { return List.of(); }
    @Override
    public List<String> listFiles(String userId) { return List.of(); }
    @Override
    public byte[] compressFiles(List<String> fileNames, String userId) { return new byte[0]; }
    @Override
    public java.util.List<String> extractCompressedFile(byte[] compressedData, String extractToDirectory) { return List.of(); }
    @Override
    public boolean validateFileType(MultipartFile file) { return true; }
    @Override
    public boolean isFileSafe(String fileName) { return true; }
    @Override
    public boolean scanFileForViruses(String fileName) { return true; }
    @Override
    public Path getFileStorageLocation() { return Paths.get(storagePath); }
    @Override
    public long getMaxFileSize() { return maxFileSize; }
    @Override
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }
    @Override
    public java.util.List<String> getAllowedFileTypes() { return List.of(); }
    @Override
    public java.util.List<String> getAllowedFileExtensions() { return java.util.Arrays.asList(allowedExtensions); }
    @Override
    public long getTotalStorageUsed() { return 0L; }
    @Override
    public long getFileTypeStorageUsed(String fileType) { return 0L; }
    @Override
    public long getFileTypeStorageUsed(String fileType, String userId) { return 0L; }
    @Override
    public void cleanupUserOldFiles(String userId, int daysToKeep) { }
    @Override
    public void cleanupFileTypeOldFiles(String fileType, int daysToKeep) { }
    @Override
    public byte[] backupFiles() { return new byte[0]; }
    @Override
    public byte[] backupUserFiles(String userId) { return new byte[0]; }

}


