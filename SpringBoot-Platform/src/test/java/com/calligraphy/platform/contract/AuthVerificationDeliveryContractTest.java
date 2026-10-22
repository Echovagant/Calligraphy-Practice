package com.calligraphy.platform.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证码发送接口的**诚实性**测试。
 *
 * <p>这个接口以前无论邮件有没有发出去，一律返回 200 + 「验证码已发送，请查收邮件」。
 * 而当前环境 `spring.mail.host` 是占位值 `smtp.example.com`，邮件永远发不出去 ——
 * 用户被引导去收一封永远不会来的邮件。
 *
 * <p>现在要求：
 * <ul>
 *   <li>发信成功 → 如实回「请查收邮件」；</li>
 *   <li>发信失败 → 如实说明验证码写在了服务端日志里，**不得**再说「已发送」；</li>
 *   <li>两种情况下验证码都必须真的写进 Redis（注册校验要靠它）。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthVerificationDeliveryContractTest {

    private static final String CODE_KEY_PREFIX = "auth:verification:code:";
    private static final String LIMIT_KEY_PREFIX = "auth:verification:limit:";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /** 把真实发信替掉，用来精确控制「发得出去 / 发不出去」两种分支。 */
    @MockBean
    private JavaMailSender mailSender;

    private final List<String> emails = new ArrayList<>();

    @BeforeEach
    void setUp() {
        emails.clear();
    }

    @AfterEach
    void tearDown() {
        // 验证码与限流 key 都要清掉，否则同一个邮箱 60 秒内第二次会被判限流
        for (String email : emails) {
            String key = email.trim().toLowerCase();
            redisTemplate.delete(CODE_KEY_PREFIX + key);
            redisTemplate.delete(LIMIT_KEY_PREFIX + key);
        }
    }

    @Test
    void mailDeliverySucceedsTellsTheUserToCheckTheirInbox() throws Exception {
        String email = newEmail();

        MvcResult result = mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andReturn();

        String message = readMessage(result);
        assertTrue(message.contains("请查收邮件"),
                "邮件确实发出去了，就应该让用户去查收，实际返回：" + message);
        assertNotNull(readCodeFromRedis(email), "验证码必须真的写进 Redis");
    }

    @Test
    void mailDeliveryFailsDoesNotClaimTheCodeWasSent() throws Exception {
        String email = newEmail();
        doThrow(new MailSendException("SMTP 不可达")).when(mailSender).send(any(SimpleMailMessage.class));

        MvcResult result = mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk())
                .andReturn();

        String message = readMessage(result);
        assertTrue(!message.contains("请查收邮件"),
                "邮件没发出去却让用户去查收，这就是报假成功，实际返回：" + message);
        assertTrue(message.contains("日志"),
                "邮件发不出去时应如实说明验证码写在了服务端日志里，实际返回：" + message);
        assertNotNull(readCodeFromRedis(email),
                "即使邮件没发出去，验证码也必须写进 Redis，否则注册校验无从进行");
    }

    @Test
    void repeatedRequestWithinTheRateLimitWindowIsRejected() throws Exception {
        String email = newEmail();

        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isOk());

        // 同一邮箱 60 秒内第二次 → 429
        mockMvc.perform(post("/api/auth/verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email))))
                .andExpect(status().isTooManyRequests());
    }

    // ------------------------------------------------------------ 工具

    private String newEmail() {
        String email = "avd_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        emails.add(email);
        return email;
    }

    private String readMessage(MvcResult result) throws Exception {
        // 必须读**原始字节**再交给 Jackson。
        // MockHttpServletResponse.getContentAsString() 会按它自己的
        // characterEncoding 解码，而这个字段的默认值是 ISO-8859-1 ——
        // 于是 UTF-8 的中文响应体被当成 Latin-1 读，断言里的中文对不上
        // （看起来像接口返回了乱码，其实字节是对的）。
        // Jackson 读 byte[] 时按 JSON 规范自己用 UTF-8 解码。
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return node.path("message").asText("");
    }

    private String readCodeFromRedis(String email) {
        return redisTemplate.opsForValue().get(CODE_KEY_PREFIX + email.trim().toLowerCase());
    }

    private static String json(Object value) throws Exception {
        return new ObjectMapper().writeValueAsString(value);
    }
}
