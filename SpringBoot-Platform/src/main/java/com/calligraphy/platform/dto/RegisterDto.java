package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "用户注册请求DTO")
public class RegisterDto {
    
    @NotBlank(message = "鐢ㄦ埛鍚嶄笉鑳戒负绌")
    @Size(min = 3, max = 20, message = "鐢ㄦ埛鍚嶉暱搴﹀繀椤诲湪3-20涓瓧绗︿箣闂")
    @Schema(description = "鐢ㄦ埛鍚", example = "user")
    private String username;
    
    @NotBlank(message = "瀵嗙爜涓嶈兘涓虹┖")
    @Size(min = 6, max = 20, message = "瀵嗙爜闀垮害蹇呴』鍦?-20涓瓧绗︿箣闂")
    @Schema(description = "密码", example = "password123")
    private String password;
    
    @NotBlank(message = "閭涓嶈兘涓虹┖")
    @Email(message = "閭鏍煎紡涓嶆纭")
    @Schema(description = "閭", example = "user@example.com")
    private String email;
    
    @NotBlank(message = "鏄电О涓嶈兘涓虹┖")
    @Size(max = 50, message = "鏄电О长度不能超过50涓瓧绗")
    @Schema(description = "鏄电О", example = "鐢ㄦ埛鏄电О")
    private String nickname;
    
    @Schema(description = "楠岃瘉鐮", example = "1234")
    private String captcha;
    
    @Schema(description = "閭€请码", example = "INVITE123")
    private String inviteCode;
}


