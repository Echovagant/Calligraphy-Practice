package com.calligraphy.platform.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface FileStorageService {
    
    // 文件上传
    String storeFile(MultipartFile file, String userId, String fileType);
    
    String storeFile(MultipartFile file, String userId, String fileType, String subDirectory);
    
    List<String> storeMultipleFiles(MultipartFile[] files, String userId, String fileType);
    
    List<String> storeMultipleFiles(MultipartFile[] files, String userId, String fileType, String subDirectory);
    
    // 文件下载
    Resource loadFileAsResource(String fileName);
    
    Resource loadFileAsResource(String fileName, String userId);
    
    byte[] loadFileAsBytes(String fileName);
    
    byte[] loadFileAsBytes(String fileName, String userId);
    
    // 文件删除
    boolean deleteFile(String fileName);
    
    boolean deleteFile(String fileName, String userId);
    
    boolean deleteMultipleFiles(List<String> fileNames);
    
    boolean deleteMultipleFiles(List<String> fileNames, String userId);
    
    void deleteUserFiles(String userId);
    
    // 鏂囦欢淇℃伅
    boolean fileExists(String fileName);
    
    boolean fileExists(String fileName, String userId);
    
    long getFileSize(String fileName);
    
    long getFileSize(String fileName, String userId);
    
    String getFileMimeType(String fileName);
    
    String getFileMimeType(String fileName, String userId);
    
    java.time.LocalDateTime getFileCreationTime(String fileName);
    
    java.time.LocalDateTime getFileCreationTime(String fileName, String userId);
    
    java.time.LocalDateTime getFileLastModifiedTime(String fileName);
    
    java.time.LocalDateTime getFileLastModifiedTime(String fileName, String userId);
    
    // 文件列表
    List<String> listFiles();
    
    List<String> listFiles(String userId);
    
    List<String> listFilesByType(String fileType);
    
    List<String> listFilesByType(String fileType, String userId);
    
    List<String> listFilesByExtension(String extension);
    
    List<String> listFilesByExtension(String extension, String userId);
    
    // 鏂囦欢澶嶅埗鍜岀Щ鍔
    String copyFile(String sourceFileName, String destinationFileName);
    
    String copyFile(String sourceFileName, String destinationFileName, String userId);
    
    String moveFile(String sourceFileName, String destinationFileName);
    
    String moveFile(String sourceFileName, String destinationFileName, String userId);
    
    // 鏂囦欢閲嶅懡鍚
    String renameFile(String oldFileName, String newFileName);
    
    String renameFile(String oldFileName, String newFileName, String userId);
    
    // 文件压缩
    byte[] compressFiles(List<String> fileNames);
    
    byte[] compressFiles(List<String> fileNames, String userId);
    
    List<String> extractCompressedFile(byte[] compressedData, String extractToDirectory);
    
    // 文件验证
    boolean validateFile(MultipartFile file);
    
    boolean validateFileSize(MultipartFile file);
    
    boolean validateFileType(MultipartFile file);
    
    boolean validateFileExtension(MultipartFile file);
    
    boolean validateFileContent(MultipartFile file);
    
    // 瀹夊叏妫€鏌
    boolean isFileSafe(String fileName);
    
    boolean scanFileForViruses(String fileName);
    
    boolean scanFileForMalware(String fileName);
    
    // 存储配置
    Path getFileStorageLocation();
    
    Path getUserFileStorageLocation(String userId);
    
    long getMaxFileSize();
    
    void setMaxFileSize(long maxFileSize);
    
    List<String> getAllowedFileTypes();
    
    void setAllowedFileTypes(List<String> allowedFileTypes);
    
    List<String> getAllowedFileExtensions();
    
    void setAllowedFileExtensions(List<String> allowedFileExtensions);
    
    // 瀛樺偍缁熻
    long getTotalStorageUsed();
    
    long getUserStorageUsed(String userId);
    
    long getFileTypeStorageUsed(String fileType);
    
    long getFileTypeStorageUsed(String fileType, String userId);
    
    // 存储清理
    void cleanupOldFiles(int daysToKeep);
    
    void cleanupUserOldFiles(String userId, int daysToKeep);
    
    void cleanupFileTypeOldFiles(String fileType, int daysToKeep);
    
    void cleanupOrphanedFiles();
    
    // 澶囦唤鍜屾仮澶
    byte[] backupFiles();
    
    byte[] backupUserFiles(String userId);
    
    void restoreFiles(byte[] backupData);
    
    void restoreUserFiles(String userId, byte[] backupData);
    
    // 文件加密
    String storeEncryptedFile(MultipartFile file, String userId, String fileType, String encryptionKey);
    
    Resource loadEncryptedFileAsResource(String fileName, String encryptionKey);
    
    byte[] loadEncryptedFileAsBytes(String fileName, String encryptionKey);
    
    // 鏂囦欢缂╃暐鍥
    String generateThumbnail(String fileName);
    
    String generateThumbnail(String fileName, String userId);
    
    Resource loadThumbnailAsResource(String fileName);
    
    Resource loadThumbnailAsResource(String fileName, String userId);
    
    // CDN集成
    String getCdnUrl(String fileName);
    
    String getCdnUrl(String fileName, String userId);
    
    void invalidateCdnCache(String fileName);
    
    void invalidateUserCdnCache(String userId);
    
    // 鏂囦欢鍏冩暟鎹
    java.util.Map<String, Object> getFileMetadata(String fileName);
    
    java.util.Map<String, Object> getFileMetadata(String fileName, String userId);
    
    void updateFileMetadata(String fileName, java.util.Map<String, Object> metadata);
    
    void updateFileMetadata(String fileName, String userId, java.util.Map<String, Object> metadata);
}


