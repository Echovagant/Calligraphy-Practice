package com.calligraphy.platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 作品图片替换表单（multipart/form-data），对应契约的 {@code WorkImageForm} schema。
 *
 * <p>multipart 字段名是 {@code image}。
 */
@Data
public class WorkImageFormDto {

    @NotNull(message = "图片不能为空")
    private MultipartFile image;
}
