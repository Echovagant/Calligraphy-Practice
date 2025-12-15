package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.FileUploadResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    FileUploadResponseDto uploadFile(MultipartFile file, String username, String fileType);
    FileUploadResponseDto uploadAvatar(MultipartFile file, String username);
    FileUploadResponseDto uploadWorkFile(MultipartFile file, String username);
    Resource loadFileAsResource(String filename, String fileType, String userId);
    boolean deleteFile(String filename, String fileType, String username);
    boolean deleteAvatar(String username);
}


