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
@Schema(description = "权限创建请求DTO")
public class PermissionCreateDto {

    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    @Schema(description = "权限名称", example = "USER_CREATE")
    private String name;

    @NotBlank(message = "权限描述不能为空")
    @Size(max = 200, message = "权限描述长度不能超过200个字符")
    @Schema(description = "权限描述", example = "创建用户的权限")
    private String description;

    @Schema(description = "权限编码", example = "user:create")
    private String code;

    @Schema(description = "父权限ID", example = "1")
    private Long parentId;

    @Schema(description = "权限类型", example = "menu")
    private String type;

    @Schema(description = "权限路径", example = "/users")
    private String path;

    @Schema(description = "权限图标", example = "user-icon")
    private String icon;

    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

    @Schema(description = "状态", example = "active")
    private String status;
}