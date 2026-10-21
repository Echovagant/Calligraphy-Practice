package com.calligraphy.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "角色创建数据传输对象")
public class RoleCreateDto {
    
    @NotBlank(message = "角色名称不能为空")
    @Size(min = 2, max = 50, message = "角色名称长度必须在2-50个字符之间")
    @Schema(description = "角色名称", example = "管理员")
    private String name;
    
    @Size(max = 200, message = "角色描述长度不能超过200个字符")
    @Schema(description = "角色描述", example = "系统管理员角色")
    private String description;
    
    @NotBlank(message = "角色编码不能为空")
    @Size(min = 2, max = 30, message = "角色编码长度必须在2-30个字符之间")
    @Schema(description = "角色编码", example = "ROLE_ADMIN")
    private String code;
    
    @Schema(description = "父角色ID", example = "parent-role-id")
    private String parentId;
    
    @Schema(description = "角色类型", example = "SYSTEM")
    private String type;
}


