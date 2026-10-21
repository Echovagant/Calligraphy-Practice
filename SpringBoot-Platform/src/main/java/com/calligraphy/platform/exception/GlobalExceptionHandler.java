package com.calligraphy.platform.exception;

import com.calligraphy.platform.dto.ApiError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一异常处理基座：把所有失败响应收敛成 {@code {code, message}} 两个字段。
 *
 * <p>状态码与错误码的对应关系见 openapi/contract.yaml 与 P2 契约切片 §0.2。
 * 未认证（401）与越权（403）发生在过滤器链里，不经过本类，分别由
 * {@code JwtAuthenticationEntryPoint} 与 {@code CustomAccessDeniedHandler} 输出。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CODE_VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String CODE_AUTH_FAILED = "AUTH_FAILED";
    private static final String CODE_UNAUTHORIZED = "UNAUTHORIZED";
    private static final String CODE_FORBIDDEN = "FORBIDDEN";
    private static final String CODE_NOT_FOUND = "NOT_FOUND";
    private static final String CODE_USER_NOT_FOUND = "USER_NOT_FOUND";
    private static final String CODE_USER_EXISTS = "USER_EXISTS";
    private static final String CODE_BUSINESS_ERROR = "BUSINESS_ERROR";
    private static final String CODE_ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
    private static final String CODE_INTERNAL_ERROR = "INTERNAL_ERROR";

    // ------------------------------------------------------------------
    // 400 参数校验
    // ------------------------------------------------------------------

    /** @Valid 校验请求体失败。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return validationError(firstFieldError(ex));
    }

    /** 表单/模型绑定校验失败。 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex) {
        return validationError(firstFieldError(ex));
    }

    /** 方法级参数（@RequestParam / @PathVariable 上的 @Validated）校验失败。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        String detail = null;
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            detail = violation.getMessage();
            break;
        }
        return validationError(detail);
    }

    /** 缺少必填请求参数。 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex) {
        return validationError(ex.getParameterName() + " 不能为空");
    }

    /** 请求参数类型不匹配（例如路径变量传了非数字）。 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return validationError(ex.getName() + " 类型不正确");
    }

    /** 请求体不是合法 JSON。 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求体解析失败: {}", ex.getMessage());
        return validationError("请求体格式错误");
    }

    /** 请求的 Content-Type 不受支持（非 JSON 等）。 */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("不支持的请求内容类型: {}", ex.getContentType());
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, CODE_VALIDATION_ERROR, "不支持的请求内容类型");
    }

    // ------------------------------------------------------------------
    // 401 / 403
    // ------------------------------------------------------------------

    /** 用户名或密码错误、令牌无效或过期等认证失败。 */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, CODE_AUTH_FAILED, messageOrDefault(ex, "认证失败"));
    }

    /** Spring Security 抛出的认证失败（如 BadCredentialsException）。 */
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleSecurityAuthentication(
            org.springframework.security.core.AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, CODE_AUTH_FAILED, messageOrDefault(ex, "认证失败"));
    }

    /** 未认证或令牌无法解析。 */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        return build(HttpStatus.UNAUTHORIZED, CODE_UNAUTHORIZED, messageOrDefault(ex, "未授权访问，请先登录"));
    }

    /** 已认证但角色/权限不足。 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, CODE_FORBIDDEN, messageOrDefault(ex, "访问被拒绝，权限不足"));
    }

    /** 账户被禁用。 */
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex) {
        return build(HttpStatus.FORBIDDEN, CODE_FORBIDDEN, messageOrDefault(ex, "账户已被禁用"));
    }

    // ------------------------------------------------------------------
    // 404
    // ------------------------------------------------------------------

    /** 资源不存在。 */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, CODE_NOT_FOUND, messageOrDefault(ex, "资源不存在"));
    }

    /** 用户不存在。 */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, CODE_USER_NOT_FOUND, messageOrDefault(ex, "用户不存在"));
    }

    /** Spring Security 的用户不存在。 */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFound(UsernameNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, CODE_USER_NOT_FOUND, messageOrDefault(ex, "用户不存在"));
    }

    /** 没有匹配的请求处理器。 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFound(NoHandlerFoundException ex) {
        return build(HttpStatus.NOT_FOUND, CODE_NOT_FOUND, "接口不存在");
    }

    /** 静态资源不存在。 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoResourceFound(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, CODE_NOT_FOUND, "资源不存在");
    }

    // ------------------------------------------------------------------
    // 409 / 422 / 423
    // ------------------------------------------------------------------

    /** 用户名或邮箱已存在。 */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, CODE_USER_EXISTS, messageOrDefault(ex, "用户已存在"));
    }

    /** 管理员用户名或邮箱已存在。 */
    @ExceptionHandler(AdminUserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAdminUserAlreadyExists(AdminUserAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, CODE_USER_EXISTS, messageOrDefault(ex, "用户已存在"));
    }

    /** 业务校验失败。 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, CODE_BUSINESS_ERROR, messageOrDefault(ex, "业务处理失败"));
    }

    /** 账户被锁定。 */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLocked(LockedException ex) {
        return build(HttpStatus.LOCKED, CODE_ACCOUNT_LOCKED, messageOrDefault(ex, "账户已被锁定"));
    }

    // ------------------------------------------------------------------
    // 其他
    // ------------------------------------------------------------------

    /** 文件上传参数不合法。 */
    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiError> handleFileUpload(FileUploadException ex) {
        return build(HttpStatus.BAD_REQUEST, CODE_VALIDATION_ERROR, messageOrDefault(ex, "文件上传失败"));
    }

    /** 请求方法不被支持。 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, CODE_VALIDATION_ERROR, "请求方法不被支持");
    }

    /** 兜底：记录日志，但不把异常细节泄露到响应体。 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception ex) {
        log.error("未处理的服务端异常", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, CODE_INTERNAL_ERROR, "服务器内部错误，请稍后重试");
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    private ResponseEntity<ApiError> validationError(String detail) {
        String message = detail == null || detail.isBlank() ? "参数校验失败" : "参数校验失败: " + detail;
        return build(HttpStatus.BAD_REQUEST, CODE_VALIDATION_ERROR, message);
    }

    private String firstFieldError(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError == null) {
            return null;
        }
        String detail = fieldError.getDefaultMessage();
        if (detail == null || detail.isBlank()) {
            detail = "值不合法";
        }
        return fieldError.getField() + " " + detail;
    }

    private String messageOrDefault(Throwable ex, String fallback) {
        String message = ex == null ? null : ex.getMessage();
        return message == null || message.isBlank() ? fallback : message;
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
