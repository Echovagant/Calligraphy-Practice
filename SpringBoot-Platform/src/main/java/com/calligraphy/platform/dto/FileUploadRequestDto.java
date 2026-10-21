package com.calligraphy.platform.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadRequestDto {
    
    @NotBlank(message = "文件名称不能为空")
    @Size(max = 255, message = "文件名称长度不能超过255个字符")
    private String fileName;
    
    @NotBlank(message = "文件类型不能为空")
    @Size(max = 100, message = "文件类型长度不能超过100个字符")
    private String fileType;
    
    @NotBlank(message = "文件用途不能为空")
    @Size(max = 50, message = "文件用途长度不能超过50个字符")
    private String filePurpose;
    
    private String relatedEntityId;
    
    private String relatedEntityType;
    
    @Size(max = 500, message = "文件描述长度不能超过500个字符")
    private String description;
    
    private boolean isPublic;
    
    private String category;
    
    private String tags;
}


