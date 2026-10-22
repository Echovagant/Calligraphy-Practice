package com.calligraphy.platform.performance;

import com.calligraphy.platform.model.BackendUser;
import com.calligraphy.platform.model.Comment;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.CommentRepository;
import com.calligraphy.platform.repository.FileReviewRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.calligraphy.platform.repository.WorkRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
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

import java.nio.charset.StandardCharsets;
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
 * P2：列表接口不许出现 N+1。
 *
 * <p>作品 / 评论 / 审核记录映射成 DTO 时都要读 LAZY 关联
 * （{@code work.getUser()}、{@code comment.getUser()/getWork()/getReviewer()}、
 * {@code review.getUploadedBy()/getReviewer()/getWork()}）。
 * 以前每条记录都要再发一次查询，一页 20 条就是几十次额外查询。
 *
 * <p>断言方式：打开 Hibernate 统计，看**实体抓取数**（{@code getFetchCount}）——
 * 它统计的是「懒加载代理被初始化」的次数，也就是 N+1 的次数。
 * 关联改成 JOIN 一次取齐之后，这个数必须是 0。
 *
 * <p>同时断言关联字段（作者昵称等）仍然是对的 —— JOIN 化不能改坏返回值。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ListAssociationFetchTest {

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
    private FileReviewRepository fileReviewRepository;

    @Autowired
    private BackendUserRepository backendUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionFactory sessionFactory;

    /** 三个作者，每人一部作品。 */
    private final List<String> authorIds = new ArrayList<>();
    private final List<String> authorUsernames = new ArrayList<>();
    private final List<String> authorNicknames = new ArrayList<>();
    private final List<Long> createdWorkIds = new ArrayList<>();
    private final List<Long> createdCommentIds = new ArrayList<>();
    private final List<String> createdReviewIds = new ArrayList<>();

    private String adminUsername;
    private String adminToken;
    /** 三个作者共用的密码。 */
    private String authorPassword;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        authorPassword = "Passw0rd" + suffix;

        // 三个作者，各一部已发布作品
        for (int i = 0; i < 3; i++) {
            String username = "n1_author" + i + "_" + suffix;
            String nickname = "作者昵称" + i;
            String id = userRepository.save(User.builder()
                    .username(username)
                    .email("n1_author" + i + "_" + suffix + "@example.com")
                    .password(passwordEncoder.encode(authorPassword))
                    .nickname(nickname)
                    .status(User.UserStatus.ACTIVE)
                    .emailVerified(true)
                    .twoFactorEnabled(false)
                    .loginCount(0L)
                    .role(User.Role.USER)
                    .build()).getId();
            authorIds.add(id);
            authorUsernames.add(username);
            authorNicknames.add(nickname);

            Work work = workRepository.save(Work.builder()
                    .title("N+1 测试作品 " + i)
                    .description("描述")
                    .content("内容")
                    .imageUrl("/api/files/n1-test.png")
                    .user(userRepository.findById(id).orElseThrow())
                    .userId(id)
                    .status(Work.WorkStatus.PUBLISHED)
                    .reviewStatus(Work.ReviewStatus.APPROVED)
                    .likeCount(0)
                    .commentCount(0)
                    .viewCount(0)
                    .isFeatured(false)
                    .build());
            createdWorkIds.add(work.getId());
        }

        // 管理员（管理端令牌必须是 userType=ADMIN）
        adminUsername = "n1_admin_" + suffix;
        userRepository.save(User.builder()
                .username(adminUsername)
                .email("n1_admin_" + suffix + "@example.com")
                .password(passwordEncoder.encode("Passw0rd" + suffix))
                .nickname("N+1 测试管理员")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.SUPER_ADMIN)
                .build());
        adminToken = loginAdmin(adminUsername, "Passw0rd" + suffix);
    }

    @AfterEach
    void tearDown() {
        for (String reviewId : createdReviewIds) {
            fileReviewRepository.findById(reviewId).ifPresent(r -> fileReviewRepository.deleteById(r.getId()));
        }
        createdReviewIds.clear();
        for (Long id : createdCommentIds) {
            commentRepository.findById(id).ifPresent(c -> commentRepository.deleteById(c.getId()));
        }
        createdCommentIds.clear();
        for (Long id : createdWorkIds) {
            workRepository.findById(id).ifPresent(w -> workRepository.deleteById(w.getId()));
        }
        createdWorkIds.clear();
        // 管理员在 backend_users 里的审核员记录（外键需要），也要清掉
        backendUserRepository.findByUsername(adminUsername)
                .ifPresent(reviewer -> backendUserRepository.delete(reviewer));
        for (String id : authorIds) {
            userRepository.findById(id).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        authorIds.clear();
        userRepository.findByUsername(adminUsername).ifPresent(u -> userRepository.deleteById(u.getId()));
    }

    // ------------------------------------------------------------ 公开画廊

    /**
     * 公开画廊：作者信息一次 JOIN 取齐，不该每条作品再查一次 users。
     */
    @Test
    void publicGalleryDoesNotFetchAuthorPerWork() throws Exception {
        sessionFactory.getStatistics().clear();

        mockMvc.perform(get("/api/works").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());

        assertEquals(0L, userFetchCount(),
                "公开画廊不该逐条作品再查一次 users（抓取了 " + userFetchCount() + " 次）");
    }

    /**
     * 「我的作品」列表：同一个 N+1 的另一条路径，昵称必须照旧是对的。
     */
    @Test
    void userWorksListDoesNotFetchAuthorPerWorkAndKeepsNickname() throws Exception {
        sessionFactory.getStatistics().clear();

        MvcResult result = mockMvc.perform(get("/api/works/user/" + authorIds.get(0))
                        .header("Authorization", "Bearer " + loginFront(authorUsernames.get(0)))
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = body(result).get("content");
        assertTrue(content.isArray() && content.size() == 1,
                "该用户名下应当正好一部作品，实际=" + content.size());
        assertEquals(authorNicknames.get(0), content.get(0).get("userNickname").asText(),
                "作者昵称必须照旧返回");
        assertEquals(authorUsernames.get(0), content.get(0).get("username").asText(),
                "作者用户名必须照旧返回");

        assertEquals(0L, userFetchCount(),
                "作品列表不该逐条再查一次 users（抓取了 " + userFetchCount() + " 次）");
    }

    // ------------------------------------------------------------ 作品评论列表

    /**
     * 作品评论列表：作者、作品、审核人三个关联一次 JOIN 取齐。
     */
    @Test
    void workCommentListDoesNotFetchAssociationsPerComment() throws Exception {
        Long workId = createdWorkIds.get(0);
        for (int i = 0; i < 3; i++) {
            User author = userRepository.findById(authorIds.get(i)).orElseThrow();
            Work work = workRepository.findById(workId).orElseThrow();
            Comment comment = commentRepository.save(Comment.builder()
                    .content("评论 " + i)
                    .user(author)
                    .work(work)
                    .likeCount(0)
                    .status(Comment.CommentStatus.APPROVED)
                    .isFeatured(false)
                    .build());
            createdCommentIds.add(comment.getId());
        }

        sessionFactory.getStatistics().clear();

        MvcResult result = mockMvc.perform(get("/api/community/comments/work/" + workId)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = body(result).get("content");
        assertEquals(3, content.size(), "三条评论都该在列表里");
        // 昵称照旧正确（JOIN 化不能改坏返回值）
        List<String> nicknames = new ArrayList<>();
        content.forEach(node -> nicknames.add(node.get("userNickname").asText()));
        for (String nickname : authorNicknames) {
            assertTrue(nicknames.contains(nickname), "评论列表里必须有昵称 " + nickname);
        }

        assertEquals(0L, userFetchCount(),
                "评论列表不该逐条再查一次 users（抓取了 " + userFetchCount() + " 次）");
        assertEquals(0L, fetchCount(Work.class),
                "评论列表不该逐条再查一次 works（抓取了 " + fetchCount(Work.class) + " 次）");
        assertEquals(0L, fetchCount(BackendUser.class),
                "评论列表不该逐条再查一次 backend_users（抓取了 " + fetchCount(BackendUser.class) + " 次）");
    }

    // ------------------------------------------------------------ 我的审核记录

    /**
     * 管理端「我的审核记录」：上传者 / 审核人 / 作品一次 JOIN 取齐。
     */
    @Test
    void myFileReviewsDoNotFetchAssociationsPerReview() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", createdWorkIds.get(0),
                                "reviewStatus", "APPROVED",
                                "reviewComments", "通过"))))
                .andExpect(status().isCreated())
                .andReturn();
        createdReviewIds.add(body(created).get("id").asText());

        sessionFactory.getStatistics().clear();

        MvcResult result = mockMvc.perform(get("/api/admin/file-reviews/my-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode content = body(result).get("content");
        assertTrue(content.isArray() && content.size() >= 1, "至少要有一条审核记录");
        assertEquals(adminUsername, content.get(0).get("reviewerUsername").asText(),
                "审核人用户名必须照旧返回");

        assertEquals(0L, userFetchCount(),
                "审核记录列表不该逐条再查一次 users（抓取了 " + userFetchCount() + " 次）");
        assertEquals(0L, fetchCount(BackendUser.class),
                "审核记录列表不该逐条再查一次 backend_users（抓取了 " + fetchCount(BackendUser.class) + " 次）");
        assertEquals(0L, fetchCount(Work.class),
                "审核记录列表不该逐条再查一次 works（抓取了 " + fetchCount(Work.class) + " 次）");
    }

    /**
     * 响应体原始字节里必须直接含有昵称的 UTF-8 字节序列 —— **字节级**证据。
     *
     * <p>MockHttpServletResponse 的 {@code characterEncoding} 默认是 ISO-8859-1，
     * {@code getContentAsString()} 会把正确的 UTF-8 响应按 Latin-1 解码成乱码。
     * 所以这里不看字符串，只看原始字节：接口吐出来的就是标准 UTF-8，
     * 与 JOIN 化无关。
     */
    @Test
    void responseBodyCarriesCorrectUtf8BytesForTheNickname() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/works/user/" + authorIds.get(0))
                        .header("Authorization", "Bearer " + loginFront(authorUsernames.get(0)))
                        .param("page", "0").param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        byte[] raw = result.getResponse().getContentAsByteArray();
        byte[] expected = authorNicknames.get(0).getBytes(StandardCharsets.UTF_8);
        byte[] head = java.util.Arrays.copyOf(raw, Math.min(200, raw.length));

        assertTrue(indexOf(raw, expected) >= 0,
                "响应体原始字节里应当直接含有昵称的 UTF-8 字节序列 " + hex(expected)
                        + "，实际响应体前 200 字节 = " + hex(head));
    }

    // ------------------------------------------------------------ 工具

    /** 字节数组里找子序列的起始下标，找不到返回 -1。 */
    private static int indexOf(byte[] haystack, byte[] needle) {
        outer:
        for (int i = 0; i + needle.length <= haystack.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private long userFetchCount() {
        return fetchCount(User.class);
    }

    /** 该实体在这段请求里被「懒加载代理初始化」的次数 —— N+1 的次数。 */
    private long fetchCount(Class<?> entityType) {
        return sessionFactory.getStatistics()
                .getEntityStatistics(entityType.getName())
                .getFetchCount();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "响应体不能为空");
        JsonNode node = objectMapper.readTree(content);
        assertNotNull(node);
        return node;
    }

    private String loginFront(String username) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", authorPassword))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }
}
