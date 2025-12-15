package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "鐢ㄦ埛鐧诲綍璇锋眰DTO")
public class LoginDto {
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌?)
    @Size(min = 3, max = 20, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-20涓瓧绗︿箣闂?)
    @Schema(description = "鐢ㄦ埛鍚?, example = "user")
    private String username;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 20, message = "瀵嗙爜闀垮害蹇呴』鍦?-20涓瓧绗︿箣闂?)
    @Schema(description = "瀵嗙爜", example = "password123")
    private String password;
    
    @Schema(description = "楠岃瘉鐮?, example = "1234")
    private String captcha;
    
    @Schema(description = "璁颁綇鎴?, example = "true")
    private Boolean rememberMe;
}


