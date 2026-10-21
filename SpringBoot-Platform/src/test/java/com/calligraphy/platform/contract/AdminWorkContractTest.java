package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
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
 * 管理端作品查询的契约测试（adminPendingWorks）。
 *
 * <p>这个端点存在的理由：公开画廊 {@code GET /api/works} 只返回「已发布且已审核通过」的作品，
 * 后台的「作品审核」列表如果也用它，**待审队列根本取不到** —— 刚上传的作品是
 * DRAFT+PENDING，永远不在列表里，审核功能等于废的。
 *
 * <p>断言只依赖契约（路径、状态码、字段名），不引用实现层的 DTO 类。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AdminWorkContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private BackendUserRepository backendUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 作品作者（普通用户），用来验证 403。 */
    private String authorUsername;
    private String authorPassword;
    private String authorToken;

    /** 管理员（SUPER_ADMIN）。 */
    private String adminUsername;
    private String adminPassword;
    private String adminToken;

    /** 待审作品：DRAFT + PENDING。 */
    private Long pendingWorkId;

    /** 已发布作品：PUBLISHED + APPROVED，不该出现在待审队列里。 */
    private Long publishedWorkId;

    private final List<Long> createdWorkIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        authorPassword = "Passw0rd" + suffix;
        authorUsername = "aw_author_" + suffix;
        String authorId = userRepository.save(User.builder()
                .username(authorUsername)
                .email("aw_author_" + suffix + "@example.com")
                .password(passwordEncoder.encode(authorPassword))
                .nickname("普通用户")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        authorToken = login(authorUsername, authorPassword);

        adminPassword = "Passw0rd" + suffix;
        adminUsername = "aw_admin_" + suffix;
        userRepository.save(User.builder()
                .username(adminUsername)
                .email("aw_admin_" + suffix + "@example.com")
                .password(passwordEncoder.encode(adminPassword))
                .nickname("契约测试管理员")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.SUPER_ADMIN)
                .build());
        adminToken = login(adminUsername, adminPassword);

        User author = userRepository.findById(authorId).orElseThrow();

        pendingWorkId = workRepository.save(Work.builder()
                .title("待审作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/aw-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();

        publishedWorkId = workRepository.save(Work.builder()
                .title("已发布作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/aw-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.PUBLISHED)
                .reviewStatus(Work.ReviewStatus.APPROVED)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();

        createdWorkIds.add(pendingWorkId);
        createdWorkIds.add(publishedWorkId);
    }

    @AfterEach
    void tearDown() {
        for (Long workId : createdWorkIds) {
            workRepository.findById(workId).ifPresent(work -> workRepository.delete(work));
        }
        createdWorkIds.clear();

        // 管理员在 backend_users 里的审核员记录（works.reviewer_id 的外键需要），也要清掉
        backendUserRepository.findByUsername(adminUsername)
                .ifPresent(reviewer -> backendUserRepository.delete(reviewer));

        userRepository.findByUsername(authorUsername).ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername(adminUsername).ifPresent(user -> userRepository.delete(user));
    }

    // ------------------------------------------------------------ 工具

    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    // ------------------------------------------------------------ 测试

    @Test
    void adminSeesPendingWorksInTheReviewQueue() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                // 分页信封
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                // 没有 data 信封
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();

        JsonNode content = body(result).get("content");
        List<String> ids = new ArrayList<>();
        for (JsonNode node : content) {
            ids.add(node.get("id").asText());
        }
        assertTrue(ids.contains(String.valueOf(pendingWorkId)),
                "待审作品应出现在待审队列里，实际=" + ids);
        assertFalse(ids.contains(String.valueOf(publishedWorkId)),
                "已发布且已审核通过的作品不该出现在待审队列里，实际=" + ids);
    }

    @Test
    void pendingWorkUsesTheContractIdField() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode first = body(result).get("content").get(0);
        assertNotNull(first, "待审队列不应为空");
        assertNotNull(first.get("id"), "作品主键字段应是 id");
        assertFalse(first.has("workId"), "不应再有 workId 字段");
        assertNotNull(first.get("title"));
        assertNotNull(first.get("status"));
        assertNotNull(first.get("reviewStatus"));
    }

    @Test
    void nonAdminCannotReadTheReviewQueue() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void anonymousCannotReadTheReviewQueue() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void pageSizeIsClampedToASaneMaximum() throws Exception {
        mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void emptyQueueIsAnEmptyListNotAnError() throws Exception {
        // 把待审作品挪走，队列就该是空列表而不是报错
        workRepository.findById(pendingWorkId).ifPresent(work -> {
            work.setStatus(Work.WorkStatus.PUBLISHED);
            work.setReviewStatus(Work.ReviewStatus.APPROVED);
            workRepository.save(work);
        });

        MvcResult result = mockMvc.perform(get("/api/admin/works/pending")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        assertEquals(0, body(result).get("content").size(), "队列清空后应返回空数组");
    }
}
