package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "刷新令牌请求DTO")
public class RefreshTokenDto {
    
    @NotBlank(message = "鍒锋柊浠ょ墝涓嶈兘涓虹┖")
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String refreshToken;
    
    @Schema(description = "璁惧淇℃伅", example = "Mozilla/5.0...")
    private String deviceInfo;
}


