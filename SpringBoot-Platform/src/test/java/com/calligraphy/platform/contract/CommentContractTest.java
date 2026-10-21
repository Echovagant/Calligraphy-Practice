package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.CommentRepository;
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
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * comments 域的契约测试（spec 阶段 5）。
 *
 * <p>每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。
 * 刻意**只依赖契约**（路径、状态码、字段名），不引用实现层的 DTO 类。
 *
 * <p>主键字段一律叫 {@code id}（契约统一，评论也不叫 commentId）。
 *
 * <p>测试造出来的数据（两个用户 + 一个作品 + 若干评论）都在 {@code tearDown} 里清干净：
 * comments 表可能被别的测试断言行数，works 表同理。
 *
 * <p>敏感词清单在这里被覆盖成固定的一个词（{@code 敏感测试词}），
 * 这样测试不依赖 application.properties 里的默认清单内容。
 */
@ActiveProfiles("test")
@SpringBootTest(properties = "comment.moderation.sensitive-words=敏感测试词")
@AutoConfigureMockMvc
class CommentContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 评论作者。 */
    private String authorId;
    private String authorUsername;
    private String authorPassword;
    private String authorToken;

    /** 另一个用户，用来测「删别人的评论」。 */
    private String otherId;
    private String otherUsername;
    private String otherPassword;
    private String otherToken;

    private Long workId;

    /** 本测试造出来的评论 id（删除是软删除，行还在库里，测试要自己清）。 */
    private final List<Long> createdCommentIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        authorUsername = "cm_a_" + suffix;
        authorPassword = "Passw0rd" + suffix;
        authorId = createUser(authorUsername, authorPassword);
        authorToken = login(authorUsername, authorPassword);

        otherUsername = "cm_b_" + suffix;
        otherPassword = "Passw0rd" + suffix;
        otherId = createUser(otherUsername, otherPassword);
        otherToken = login(otherUsername, otherPassword);

        workId = workRepository.save(Work.builder()
                .title("契约测试作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/contract.png")
                .user(userRepository.findById(authorId).orElseThrow())
                .status(Work.WorkStatus.PUBLISHED)
                .reviewStatus(Work.ReviewStatus.APPROVED)
                .build()).getId();
    }

    @AfterEach
    void tearDown() {
        // 先清评论，再清作品，最后清用户 —— 顺序反了会撞外键
        for (Long id : createdCommentIds) {
            commentRepository.findById(id).ifPresent(c -> commentRepository.deleteById(c.getId()));
        }
        createdCommentIds.clear();
        if (workId != null) {
            workRepository.findById(workId).ifPresent(w -> workRepository.deleteById(w.getId()));
        }
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        if (otherId != null) {
            userRepository.findById(otherId).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
    }

    // ------------------------------------------------------------ commentCreate

    @Test
    void commentCreateReturns201WithContractFields() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", "写得真好"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.workId").value(String.valueOf(workId)))
                .andExpect(jsonPath("$.workTitle").value("契约测试作品"))
                .andExpect(jsonPath("$.userId").value(authorId))
                .andExpect(jsonPath("$.username").value(authorUsername))
                .andExpect(jsonPath("$.content").value("写得真好"))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.likeCount").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.replies").isArray())
                .andExpect(jsonPath("$.replyCount").value(0))
                .andExpect(jsonPath("$.hasReplies").value(false))
                // 主键叫 id，不是 commentId
                .andExpect(jsonPath("$.commentId").doesNotExist())
                // 成功响应是裸对象，没有 data 信封
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
        createdCommentIds.add(Long.valueOf(body(result).get("id").asText()));
    }

    @Test
    void anonymousCommentCreateIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/community/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", "匿名评论"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void commentOnMissingWorkReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", "999999999",
                                "content", "给不存在的作品评论"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void commentWithBlankContentIsRejected() throws Exception {
        mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ commentListByWork

    @Test
    void commentListByWorkReturnsPageEnvelope() throws Exception {
        createComment(authorToken, "作品评论");

        MvcResult result = mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .header("Authorization", "Bearer " + authorToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").isNotEmpty())
                .andExpect(jsonPath("$.content[0].content").value("作品评论"))
                .andExpect(jsonPath("$.content[0].workId").value(String.valueOf(workId)))
                // 没有 data 信封
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result));
    }

    @Test
    void commentListByMissingWorkReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/community/comments/work/999999999")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void commentListPageSizeIsClampedToASaneMaximum() throws Exception {
        mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .header("Authorization", "Bearer " + authorToken)
                        .param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    // ------------------------------------------------------------ commentListByUser

    @Test
    void commentListByUserReturnsPageEnvelope() throws Exception {
        createComment(authorToken, "用户评论");

        mockMvc.perform(get("/api/community/comments/user/" + authorId)
                        .header("Authorization", "Bearer " + authorToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].content").value("用户评论"))
                .andExpect(jsonPath("$.content[0].userId").value(authorId))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void commentListByMissingUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/community/comments/user/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void commentListByUserNeedsToken() throws Exception {
        mockMvc.perform(get("/api/community/comments/user/" + authorId))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ commentDelete

    @Test
    void commentDeleteReturns204AndRemovesTheComment() throws Exception {
        String commentId = createComment(authorToken, "待删除的评论");

        MvcResult deleted = mockMvc.perform(delete("/api/community/comments/" + commentId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNoContent())
                .andReturn();
        assertEquals("", deleted.getResponse().getContentAsString(), "204 不该有响应体");

        // 删掉之后列表里就没有它了
        mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void deletingSomeoneElsesCommentIsForbidden() throws Exception {
        String commentId = createComment(authorToken, "别人的评论");

        mockMvc.perform(delete("/api/community/comments/" + commentId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void deletingMissingCommentReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/community/comments/999999999")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void anonymousCommentDeleteIsUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/community/comments/999999999"))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------ 子评论

    /**
     * 子评论嵌在父评论的 {@code replies} 里，不单独占列表条目，
     * 汇总字段 replyCount / hasReplies 与之一致。
     */
    @Test
    void replyIsNestedUnderItsParentComment() throws Exception {
        String parentId = createComment(authorToken, "根评论");

        MvcResult reply = mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", "回复内容",
                                "parentCommentId", parentId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentCommentId").value(parentId))
                .andReturn();
        createdCommentIds.add(Long.valueOf(body(reply).get("id").asText()));

        mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                // 只有根评论占一个列表条目
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.content[0].replyCount").value(1))
                .andExpect(jsonPath("$.content[0].hasReplies").value(true))
                .andExpect(jsonPath("$.content[0].replies").isArray())
                .andExpect(jsonPath("$.content[0].replies[0].content").value("回复内容"))
                .andExpect(jsonPath("$.content[0].replies[0].parentCommentId").value(parentId));
    }

    // ------------------------------------------------------------ 内容审核

    /**
     * 命中敏感词的评论不能自动放行：status 如实返回 PENDING，
     * 而且不该出现在公开的作品评论列表里。
     */
    @Test
    void sensitiveCommentStaysPendingAndIsNotPubliclyListed() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", "这里有敏感测试词"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        createdCommentIds.add(Long.valueOf(body(result).get("id").asText()));

        mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    // ------------------------------------------------------------ 错误路径

    @Test
    void nonNumericWorkIdIsAClientErrorNotAServerError() throws Exception {
        mockMvc.perform(get("/api/community/comments/work/not-a-number")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(result -> assertTrue(result.getResponse().getStatus() < 500,
                        "非数字 workId 不该是 5xx，实际=" + result.getResponse().getStatus()));
    }

    // ------------------------------------------------------------ 工具

    private String createUser(String username, String password) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("契约测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
    }

    /** 走真实的登录端点拿令牌，认证链本身就是被测对象。 */
    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = body(login).get("accessToken").asText();
        assertFalse(accessToken.isBlank(), "登录必须拿到 accessToken");
        return accessToken;
    }

    /** 发一条评论，返回它的 id。 */
    private String createComment(String token, String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/community/comments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", String.valueOf(workId),
                                "content", content))))
                .andExpect(status().isCreated())
                .andReturn();
        String id = body(result).get("id").asText();
        createdCommentIds.add(Long.valueOf(id));
        return id;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }
}
