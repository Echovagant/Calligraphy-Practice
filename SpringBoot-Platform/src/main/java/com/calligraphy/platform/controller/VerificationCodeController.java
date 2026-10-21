package com.calligraphy.platform.controller;

import com.calligraphy.platform.dto.VerificationCodeRequestDto;
import com.calligraphy.platform.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 邮箱验证码接口，对应契约的 authSendVerificationCode operation。
 *
 * <p>契约要求：邮箱格式非法 400、60 秒内重复请求 429、成功 200 且不返回验证码本身。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationCodeController {

    private final AuthService authService;

    /**
     * POST /api/auth/verification-code —— 200 VerificationCodeSent
     *
     * <p>无论邮箱是否已注册都返回同样的 200 与 message，不泄露注册情况；
     * 触发限流时手工返回 429（基座的异常映射里没有 429 规则）。
     */
    @PostMapping("/verification-code")
    public ResponseEntity<Map<String, String>> verificationCode(
            @Valid @RequestBody VerificationCodeRequestDto verificationCodeRequest) {
        boolean sent = authService.sendVerificationCode(verificationCodeRequest.getEmail());
        if (!sent) {
            Map<String, String> body = new LinkedHashMap<>();
            body.put("code", "TOO_MANY_REQUESTS");
            body.put("message", "请求过于频繁，请稍后再试");
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
        }

        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", "验证码已发送，请查收邮件");
        return ResponseEntity.ok(body);
    }
}
