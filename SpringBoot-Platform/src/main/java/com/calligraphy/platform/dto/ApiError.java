package com.calligraphy.platform.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一失败响应体。
 *
 * <p>契约（openapi/contract.yaml 的 ApiError schema）规定失败响应体只有
 * {@code code} 与 {@code message} 两个字段，且两者都是必填。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"code", "message"})
public class ApiError {

    /** 语义化错误码，例如 AUTH_FAILED、USER_EXISTS、VALIDATION_ERROR。 */
    private String code;

    /** 可读错误消息，不允许包含堆栈等内部细节。 */
    private String message;
}
