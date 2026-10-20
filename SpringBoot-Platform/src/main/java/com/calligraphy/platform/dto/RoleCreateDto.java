package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "角色创建数据传输对象")
public class RoleCreateDto {
    
    @NotBlank(message = "瑙掕壊鍚嶇О涓嶈兘涓虹┖")
    @Size(min = 2, max = 50, message = "瑙掕壊鍚嶇О闀垮害蹇呴』鍦?-50涓瓧绗︿箣闂")
    @Schema(description = "瑙掕壊鍚嶇О", example = "绠＄悊鍛")
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200涓瓧绗")
    @Schema(description = "角色描述", example = "绯荤粺绠＄悊鍛樿鑹")
    private String description;
    
    @NotBlank(message = "瑙掕壊缂栫爜涓嶈兘涓虹┖")
    @Size(min = 2, max = 30, message = "瑙掕壊缂栫爜闀垮害蹇呴』鍦?-30涓瓧绗︿箣闂")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String code;
    
    @Schema(description = "鐖惰鑹睮D", example = "parent-role-id")
    private String parentId;
    
    @Schema(description = "角色类型", example = "SYSTEM")
    private String type;
}


