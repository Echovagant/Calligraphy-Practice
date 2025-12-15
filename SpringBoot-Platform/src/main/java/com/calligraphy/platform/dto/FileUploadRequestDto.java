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
    
    @NotBlank(message = "鏂囦欢鍚嶇О涓嶈兘涓虹┖")
    @Size(max = 255, message = "鏂囦欢鍚嶇О闀垮害涓嶈兘瓒呰繃255浣?)
    private String fileName;
    
    @NotBlank(message = "鏂囦欢绫诲瀷涓嶈兘涓虹┖")
    @Size(max = 100, message = "鏂囦欢绫诲瀷闀垮害涓嶈兘瓒呰繃100浣?)
    private String fileType;
    
    @NotBlank(message = "鏂囦欢鐢ㄩ€斾笉鑳戒负绌?)
    @Size(max = 50, message = "鏂囦欢鐢ㄩ€旈暱搴︿笉鑳借秴杩?0浣?)
    private String filePurpose;
    
    private String relatedEntityId;
    
    private String relatedEntityType;
    
    @Size(max = 500, message = "鏂囦欢鎻忚堪闀垮害涓嶈兘瓒呰繃500浣?)
    private String description;
    
    private boolean isPublic;
    
    private String category;
    
    private String tags;
}


