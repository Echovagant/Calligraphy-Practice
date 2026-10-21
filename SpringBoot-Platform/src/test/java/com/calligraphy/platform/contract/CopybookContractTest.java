package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.Copybook;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.CopybookRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * copybooks + practice 域的契约测试（spec 阶段 4）。
 *
 * <p>每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。
 * 刻意**只依赖契约**（路径、状态码、字段名），不引用实现层的 DTO 类。
 *
 * <p>主键字段一律叫 {@code id}（契约统一，practice 记录也不叫 recordId）。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CopybookContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CopybookRepository copybookRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String userId;
    private String username;
    private String email;
    private String password;
    private String accessToken;

    private String copybookId;

    /**
     * 本测试创建的字帖 ID。copybooks 表里那 3 行播种数据是事实契约
     * （CopybookRepositoryTest 断言恰好 3 行），测试**必须**把自己造的字帖清干净，
     * 否则会污染其它测试。
     */
    private final List<String> createdCopybookIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        password = "Passw0rd" + suffix;
        username = "cb_" + suffix;
        email = "cb_" + suffix + "@example.com";
        userId = userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .nickname("契约测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        // 走真实的登录端点拿令牌，认证链本身就是被测对象
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = body(login).get("accessToken").asText();
        assertFalse(accessToken.isBlank(), "登录必须拿到 accessToken");

        // 建一个字帖供练习记录引用
        copybookId = copybookRepository.save(Copybook.builder()
                .title("契约测试字帖")
                .description("描述")
                .imageUrl("/api/files/copybook.png")
                .difficulty("beginner")
                .category("楷书")
                .characterCount(100)
                .usageCount(0)
                .isActive(true)
                .build()).getId();
        createdCopybookIds.add(copybookId);
    }

    @AfterEach
    void tearDown() {
        // practice_records.user_id 有 ON DELETE CASCADE，删用户时练习记录一起清掉
        if (userId != null) {
            userRepository.findById(userId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        for (String id : createdCopybookIds) {
            copybookRepository.findById(id).ifPresent(c -> copybookRepository.deleteById(c.getId()));
        }
        createdCopybookIds.clear();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    // ------------------------------------------------------------ 3 个公开 operation

    @Test
    void copybookListIsPublicAndPaged() throws Exception {
        mockMvc.perform(get("/api/copybooks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void copybookListFiltersByCategoryAndDifficulty() throws Exception {
        mockMvc.perform(get("/api/copybooks")
                        .param("category", "楷书")
                        .param("difficulty", "beginner")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("楷书"))
                .andExpect(jsonPath("$.content[0].difficulty").value("beginner"));
    }

    @Test
    void copybookPopularIsPublicAndPaged() throws Exception {
        mockMvc.perform(get("/api/copybooks/popular")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    /**
     * 字面量路径 /popular 与模板路径 /{copybookId} 必须互不吞掉 ——
     * 契约里明确写了这条，Spring 的字面量优先规则要靠测试锁住。
     */
    @Test
    void popularPathAndTemplatePathDoNotSwallowEachOther() throws Exception {
        // /popular 走字面量路径 → 分页信封
        MvcResult popular = mockMvc.perform(get("/api/copybooks/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertNotNull(body(popular).get("content"), "/popular 必须返回分页信封");

        // /{copybookId} 走模板路径 → 裸资源（没有 content 字段）
        MvcResult detail = mockMvc.perform(get("/api/copybooks/" + copybookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(copybookId))
                .andExpect(jsonPath("$.content").doesNotExist())
                .andReturn();
        assertNotNull(body(detail).get("id"), "/{copybookId} 必须返回裸字帖");
    }

    @Test
    void copybookGetByIdIsPublic() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/copybooks/" + copybookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(copybookId))
                .andExpect(jsonPath("$.title").value("契约测试字帖"))
                .andExpect(jsonPath("$.difficulty").value("beginner"))
                .andExpect(jsonPath("$.category").value("楷书"))
                .andExpect(jsonPath("$.characterCount").value(100))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    // ------------------------------------------------------------ 3 个「当前用户」operation

    @Test
    void copybookUserStatisticsNeedsToken() throws Exception {
        mockMvc.perform(get("/api/copybooks/statistics/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPractices").isNumber())
                .andExpect(jsonPath("$.totalDuration").isNumber())
                .andExpect(jsonPath("$.averageDuration").isNumber())
                .andExpect(jsonPath("$.streakDays").isNumber())
                .andExpect(jsonPath("$.characterCount").isNumber())
                .andExpect(jsonPath("$.copybookCount").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void copybookPracticeHistoryNeedsToken() throws Exception {
        mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void copybookRecordPracticeReturns201WithContractFields() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/copybooks/" + copybookId + "/practice")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "一次练习",
                                "content", "练习内容",
                                "category", "楷书",
                                "style", "欧体",
                                "practiceType", "临摹",
                                "duration", 1800,
                                "characterCount", 42,
                                "difficulty", "beginner",
                                "notes", "写得不错",
                                "fileUrls", List.of("/api/files/practice.png")))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.title").value("一次练习"))
                // 契约字段是 duration（秒）与 characterCount，不是 practiceTime / charactersPracticed
                .andExpect(jsonPath("$.duration").value(1800))
                .andExpect(jsonPath("$.characterCount").value(42))
                .andExpect(jsonPath("$.fileUrl").value("/api/files/practice.png"))
                .andExpect(jsonPath("$.notes").value("写得不错"))
                // 主键叫 id，不是 recordId
                .andExpect(jsonPath("$.recordId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void recordedPracticeShowsUpInHistoryAndStatistics() throws Exception {
        mockMvc.perform(post("/api/copybooks/" + copybookId + "/practice")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "title", "计入统计的练习",
                                "duration", 600,
                                "characterCount", 10))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/copybooks/practice/history")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.content[0].title").value("计入统计的练习"))
                .andExpect(jsonPath("$.content[0].duration").value(600))
                .andExpect(jsonPath("$.content[0].characterCount").value(10));

        mockMvc.perform(get("/api/copybooks/statistics/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPractices").value(1))
                .andExpect(jsonPath("$.totalDuration").value(600))
                .andExpect(jsonPath("$.characterCount").value(10))
                .andExpect(jsonPath("$.copybookCount").value(1));
    }

    @Test
    void recordingPracticeBumpsCopybookUsageCount() throws Exception {
        int before = copybookRepository.findById(copybookId).orElseThrow().getUsageCount();

        mockMvc.perform(post("/api/copybooks/" + copybookId + "/practice")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "用一次"))))
                .andExpect(status().isCreated());

        int after = copybookRepository.findById(copybookId).orElseThrow().getUsageCount();
        assertEquals(before + 1, after, "记录一次练习，字帖使用次数应 +1");
    }

    // ------------------------------------------------------------ 错误路径

    @Test
    void privateCopybookEndpointsRejectAnonymous() throws Exception {
        mockMvc.perform(get("/api/copybooks/statistics/user"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/copybooks/practice/history"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/copybooks/" + copybookId + "/practice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "匿名练习"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingCopybookReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/copybooks/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void recordingPracticeOnMissingCopybookReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/copybooks/00000000-0000-0000-0000-000000000000/practice")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "给不存在的字帖记练习"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void negativeDurationIsRejectedAsValidationError() throws Exception {
        mockMvc.perform(post("/api/copybooks/" + copybookId + "/practice")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "负时长", "duration", -1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listPageSizeIsClampedToASaneMaximum() throws Exception {
        mockMvc.perform(get("/api/copybooks").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void statisticsForAUserWithNoPracticeIsAllZero() throws Exception {
        mockMvc.perform(get("/api/copybooks/statistics/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPractices").value(0))
                .andExpect(jsonPath("$.totalDuration").value(0))
                .andExpect(jsonPath("$.characterCount").value(0))
                .andExpect(jsonPath("$.copybookCount").value(0));
    }

    @Test
    void listReturnsOnlyActiveCopybooks() throws Exception {
        // 下架的字帖不该出现在列表里
        String offShelfId = copybookRepository.save(Copybook.builder()
                .title("已下架字帖")
                .imageUrl("/api/files/off.png")
                .difficulty("advanced")
                .category("草书")
                .isActive(false)
                .build()).getId();
        createdCopybookIds.add(offShelfId);

        MvcResult result = mockMvc.perform(get("/api/copybooks").param("size", "100"))
                .andExpect(status().isOk())
                .andReturn();
        for (JsonNode node : body(result).get("content")) {
            assertTrue(node.get("isActive").asBoolean(),
                    "列表里出现了已下架的字帖: " + node.get("title").asText());
        }
        assertTrue(body(result).get("content").size() > 0, "列表不该是空的（库里至少有一个启用字帖）");
    }
}
