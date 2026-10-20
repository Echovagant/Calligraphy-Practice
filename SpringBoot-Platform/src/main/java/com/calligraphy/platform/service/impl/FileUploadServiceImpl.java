package com.calligraphy.platform.service.impl;

import com.calligraphy.platform.dto.FileUploadResponseDto;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.exception.FileUploadException;
import com.calligraphy.platform.exception.ResourceNotFoundException;
import com.calligraphy.platform.service.FileUploadService;
import com.calligraphy.platform.service.SystemConfigService;
import com.calligraphy.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    
    private final SystemConfigService systemConfigService;
    private final UserService userService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB
    private long maxFileSize;
    @Override
    @Transactional
    @SuppressWarnings("null")
    public FileUploadResponseDto uploadFile(MultipartFile file, String username, String fileType) {
        log.info("文件上传: 用户={}, 鏂囦欢鍚?{}, 类型={}", username, file.getOriginalFilename(), fileType);
        
        // 验证文件
        validateFile(file, fileType);
        
        // 鑾峰彇鐢ㄦ埛淇℃伅
        User user = userService.findByUsername(username);
        
        // 鐢熸垚鍞竴鏂囦欢鍚
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(fileExtension);
        
        // 鍒涘缓涓婁紶鐩綍
        Path uploadPath = createUploadPath(user.getId(), fileType);
        
        try {
            // 纭繚鐩綍瀛樺湪
            Files.createDirectories(uploadPath);
            
            // 保存文件
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // 构建文件访问URL
            String fileUrl = buildFileUrl(user.getId(), fileType, uniqueFilename);
            
            log.info("文件上传成功: 用户={}, 鏂囦欢鍚?{}, 璺緞={}", username, uniqueFilename, filePath);
            
            return new FileUploadResponseDto(uniqueFilename, fileUrl, fileType, file.getSize(), "success");
                
        } catch (IOException e) {
            log.error("文件上传失败: 用户={}, 閿欒={}", username, e.getMessage());
            throw new FileUploadException("文件上传失败: " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public FileUploadResponseDto uploadAvatar(MultipartFile file, String username) {
        log.info("头像上传: 用户={}, 鏂囦欢鍚?{}", username, file.getOriginalFilename());
        
        // 澶村儚鍙厑璁稿浘鐗囨牸寮
        if (!isImageFile(file)) {
            throw new FileUploadException("澶村儚蹇呴』鏄浘鐗囨枃浠");
        }
        
        FileUploadResponseDto response = uploadFile(file, username, "avatar");
        
        // 更新用户头像URL
        User user = userService.findByUsername(username);
        com.calligraphy.platform.dto.FrontendUserUpdateDto dto = com.calligraphy.platform.dto.FrontendUserUpdateDto.builder()
            .userId(Long.valueOf(user.getId()))
            .avatar(response.getFileUrl())
            .build();
        userService.updateUser(user.getId(), dto);
        
        log.info("澶村儚涓婁紶骞舵洿鏂版垚鍔? 用户={}, 头像URL={}", username, response.getFileUrl());
        
        return response;
    }
    @Override
    @Transactional
    public FileUploadResponseDto uploadWorkFile(MultipartFile file, String username) {
        log.info("作品文件上传: 用户={}, 鏂囦欢鍚?{}", username, file.getOriginalFilename());
        
        // 浣滃搧鏂囦欢鍙厑璁稿浘鐗囧拰PDF格式
        if (!isAllowedWorkFile(file)) {
            throw new FileUploadException("浣滃搧鏂囦欢蹇呴』鏄浘鐗囨垨PDF格式");
        }
        
        return uploadFile(file, username, "work");
    }
    @Override
    @SuppressWarnings("null")
    public Resource loadFileAsResource(String filename, String fileType, String userId) {
        log.info("加载文件资源: 鏂囦欢鍚?{}, 类型={}, 用户ID={}", filename, fileType, userId);
        
        try {
            Path filePath = createUploadPath(userId, fileType).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("鏂囦欢涓嶅瓨鍦? " + filename);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("鏂囦欢璺緞閿欒: " + filename);
        }
    }
    @Override
    @Transactional
    public boolean deleteFile(String filename, String fileType, String username) {
        log.info("删除文件: 鏂囦欢鍚?{}, 类型={}, 用户={}", filename, fileType, username);
        
        try {
            User user = userService.findByUsername(username);
            Path filePath = createUploadPath(user.getId(), fileType).resolve(filename).normalize();
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: 鏂囦欢鍚?{}, 用户={}", filename, username);
                return true;
            } else {
                log.warn("鏂囦欢涓嶅瓨鍦? 鏂囦欢鍚?{}, 用户={}", filename, username);
                return false;
            }
        } catch (IOException e) {
            log.error("文件删除失败: 鏂囦欢鍚?{}, 用户={}, 閿欒={}", filename, username, e.getMessage());
            throw new FileUploadException("文件删除失败: " + e.getMessage());
        }
    }
    @Override
    public boolean deleteAvatar(String username) {
        log.info("删除头像: 用户={}", username);
        
        User user = userService.findByUsername(username);
        String avatarUrl = user.getAvatarUrl();
        
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            // 浠嶶RL涓彁鍙栨枃浠跺悕
            String filename = extractFilenameFromUrl(avatarUrl);
            boolean deleted = deleteFile(filename, "avatar", username);
            
            if (deleted) {
                // 更新用户头像URL涓虹┖
                com.calligraphy.platform.dto.FrontendUserUpdateDto dto = com.calligraphy.platform.dto.FrontendUserUpdateDto.builder()
                    .userId(Long.valueOf(user.getId()))
                    .avatar(null)
                    .build();
                userService.updateUser(user.getId(), dto);
                log.info("澶村儚鍒犻櫎骞舵洿鏂版垚鍔? 用户={}", username);
            }
            
            return deleted;
        }
        
        return false;
    }

    private void validateFile(MultipartFile file, String fileType) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("鏂囦欢涓嶈兘涓虹┖");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new FileUploadException("鏂囦欢澶у皬瓒呰繃闄愬埗: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new FileUploadException("鏂囦欢鍚嶄笉鑳戒负绌");
        }
        
        // 妫€鏌ユ枃浠跺悕鏄惁鍖呭惈璺緞閬嶅巻瀛楃
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new FileUploadException("鏂囦欢鍚嶅寘鍚潪娉曞瓧绗");
        }
        
        // 根据文件类型验证文件格式
        switch (fileType) {
            case "avatar":
                if (!isImageFile(file)) {
                    throw new FileUploadException("澶村儚蹇呴』鏄浘鐗囨枃浠");
                }
                break;
            case "work":
                if (!isAllowedWorkFile(file)) {
                    throw new FileUploadException("浣滃搧鏂囦欢鏍煎紡涓嶆敮鎸");
                }
                break;
            default:
                if (!isAllowedFile(file)) {
                    throw new FileUploadException("鏂囦欢鏍煎紡涓嶆敮鎸");
                }
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif");
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif");
        
        return allowedTypes.contains(contentType) && allowedExtensions.contains(extension.toLowerCase());
    }

    private boolean isAllowedWorkFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        List<String> allowedTypes = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/gif", "application/pdf");
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf");
        
        return allowedTypes.contains(contentType) && allowedExtensions.contains(extension.toLowerCase());
    }

    private boolean isAllowedFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        
        // 浠庣郴缁熼厤缃幏鍙栧厑璁哥殑鏂囦欢绫诲瀷
        try {
            String allowedTypes = systemConfigService.getConfigValue("work.upload.allowed.types");
            List<String> allowedExtensions = Arrays.asList(allowedTypes.split(","));
            return allowedExtensions.contains(extension.toLowerCase());
        } catch (Exception e) {
            // 濡傛灉閰嶇疆涓嶅瓨鍦紝浣跨敤榛樿配置
            List<String> defaultExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf");
            return defaultExtensions.contains(extension.toLowerCase());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String generateUniqueFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    private Path createUploadPath(String userId, String fileType) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return Paths.get(uploadDir, fileType, userId, datePath);
    }

    private String buildFileUrl(String userId, String fileType, String filename) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/api/files/%s/%s/%s/%s", fileType, userId, datePath, filename);
    }

    private String extractFilenameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        // 浠嶶RL涓彁鍙栨枃浠跺悕
        String[] parts = url.split("/");
        return parts.length > 0 ? parts[parts.length - 1] : "";
    }
}


