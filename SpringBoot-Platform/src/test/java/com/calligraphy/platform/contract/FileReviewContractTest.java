package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.User;
import com.calligraphy.platform.model.Work;
import com.calligraphy.platform.repository.BackendUserRepository;
import com.calligraphy.platform.repository.FileReviewRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * file-reviews 域的契约测试（P5 阶段 5）。
 *
 * <p>每个 operation 一个测试方法，断言 method / path / 状态码 / 响应字段集 / 错误码。
 * 刻意**只依赖契约**（路径、状态码、字段名），不引用实现层的 DTO 类。
 *
 * <p>主键字段一律叫 {@code id}；单资源是裸对象，列表是分页信封，
 * 两者都**没有** data 信封。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class FileReviewContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private FileReviewRepository fileReviewRepository;

    @Autowired
    private BackendUserRepository backendUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 作品作者（普通用户），用来验证 403。 */
    private String authorId;
    private String authorUsername;
    private String authorPassword;
    private String authorToken;

    /** 管理员（SUPER_ADMIN），用来调用 /api/admin/file-reviews。 */
    private String adminUsername;
    private String adminPassword;
    private String adminToken;

    private Long workId;

    /** 本测试创建的审核记录 ID，@AfterEach 必须清干净。 */
    private final List<String> createdReviewIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        authorPassword = "Passw0rd" + suffix;
        authorUsername = "fr_author_" + suffix;
        authorId = userRepository.save(User.builder()
                .username(authorUsername)
                .email("fr_author_" + suffix + "@example.com")
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
        adminUsername = "fr_admin_" + suffix;
        userRepository.save(User.builder()
                .username(adminUsername)
                .email("fr_admin_" + suffix + "@example.com")
                .password(passwordEncoder.encode(adminPassword))
                .nickname("契约测试管理员")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.SUPER_ADMIN)
                .build());
        // 管理端接口只认 ADMIN 类型的令牌（userType=ADMIN），必须走后台登录端点
        adminToken = loginAdmin(adminUsername, adminPassword);

        Work work = workRepository.save(Work.builder()
                .title("契约测试作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/fr-contract-test.png")
                .user(userRepository.findById(authorId).orElseThrow())
                .userId(authorId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build());
        workId = work.getId();
    }

    @AfterEach
    void tearDown() {
        for (String reviewId : createdReviewIds) {
            fileReviewRepository.findById(reviewId).ifPresent(review -> fileReviewRepository.delete(review));
        }
        createdReviewIds.clear();

        // 断言中途失败时可能还有没登记的审核记录：按作品兜底清一次。
        // file_reviews.work_id 指向 works，审核记录必须先删。
        fileReviewRepository.findByWorkId(workId)
                .forEach(review -> fileReviewRepository.delete(review));

        // works.reviewer_id 指向 backend_users，作品必须先删。
        workRepository.findById(workId).ifPresent(work -> workRepository.delete(work));

        // 管理员在 backend_users 里的审核员记录（外键需要），也要清掉。
        backendUserRepository.findByUsername(adminUsername)
                .ifPresent(reviewer -> backendUserRepository.delete(reviewer));

        userRepository.findByUsername(authorUsername).ifPresent(user -> userRepository.delete(user));
        userRepository.findByUsername(adminUsername).ifPresent(user -> userRepository.delete(user));
    }

    // ------------------------------------------------------------ 工具

    /** 走真实的登录端点拿令牌，认证链本身就是被测对象。 */
    private String login(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String token = body(login).get("accessToken").asText();
        assertFalse(token.isBlank(), "登录必须拿到 accessToken");
        return token;
    }

    /** 管理端令牌：必须是 userType=ADMIN 的令牌，前端登录发的 USER 令牌进不了管理端。 */
    private String loginAdmin(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String token = body(login).get("accessToken").asText();
        assertFalse(token.isBlank(), "登录必须拿到 accessToken");
        return token;
    }

    /** 提交一条审核并返回审核记录 ID。 */
    private String createReview(String reviewStatus, String reviewComments) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", workId,
                                "reviewStatus", reviewStatus,
                                "reviewComments", reviewComments))))
                .andExpect(status().isCreated())
                .andReturn();
        String reviewId = body(result).get("id").asText();
        createdReviewIds.add(reviewId);
        return reviewId;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    // ------------------------------------------------------------ fileReviewCreate

    @Test
    void createReviewReturns201WithContractFields() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", workId,
                                "reviewStatus", "APPROVED",
                                "reviewComments", "字写得不错"))))
                .andExpect(status().isCreated())
                // 契约字段：主键叫 id，不是 reviewId
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.reviewId").doesNotExist())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.reviewComment").value("字写得不错"))
                .andExpect(jsonPath("$.relatedWorkId").value(String.valueOf(workId)))
                .andExpect(jsonPath("$.relatedWorkTitle").value("契约测试作品"))
                .andExpect(jsonPath("$.reviewerUsername").value(adminUsername))
                .andExpect(jsonPath("$.userId").value(authorId))
                .andExpect(jsonPath("$.username").value(authorUsername))
                .andExpect(jsonPath("$.totalReviews").value(1))
                .andExpect(jsonPath("$.approvedCount").value(1))
                .andExpect(jsonPath("$.approvalRate").value(1.0))
                .andExpect(jsonPath("$.averageReviewTime").isNumber())
                // 没有 data 信封
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        createdReviewIds.add(body(result).get("id").asText());
    }

    @Test
    void createReviewOnMissingWorkReturns404() throws Exception {
        mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("workId", 987654321L, "reviewStatus", "APPROVED"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void createReviewWithUnknownStatusIsRejectedAsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("workId", workId, "reviewStatus", "WHATEVER"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ works.review_status 回写

    @Test
    void createReviewWritesReviewStatusBackToTheWork() throws Exception {
        createReview("APPROVED", "通过");

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.APPROVED, work.getReviewStatus(), "审核结论必须回写 works.review_status");
        assertEquals(Work.WorkStatus.PUBLISHED, work.getStatus(), "审核通过后作品应该进入公开状态");

        // 公开的作品详情接口也能看到回写结果
        mockMvc.perform(get("/api/works/" + workId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewStatus").value("APPROVED"));
    }

    @Test
    void updateReviewWritesReviewStatusBackToTheWork() throws Exception {
        String reviewId = createReview("PENDING", "先看看");

        mockMvc.perform(put("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewStatus", "REJECTED", "reviewComments", "再练练"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.data").doesNotExist());

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.REJECTED, work.getReviewStatus(), "更新结论后必须回写 works.review_status");
    }

    // ------------------------------------------------------------ fileReviewBatch

    @Test
    void batchReviewKeepsGoingWhenOneWorkIsMissing() throws Exception {
        long missingWorkId = 987654321L;

        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews/batch")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workIds", List.of(workId, missingWorkId),
                                "reviewStatus", "APPROVED",
                                "reviewComments", "批量通过"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.succeeded").value(1))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.reviews").isArray())
                .andExpect(jsonPath("$.reviews.length()").value(1))
                .andExpect(jsonPath("$.reviews[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.reviews[0].relatedWorkId").value(String.valueOf(workId)))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();

        createdReviewIds.add(body(result).get("reviews").get(0).get("id").asText());

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.APPROVED, work.getReviewStatus(), "批量审核同样要回写 works.review_status");
    }

    // ------------------------------------------------------------ fileReviewMyReviews

    @Test
    void myReviewsReturnsPageEnvelopeOfCurrentAdminReviews() throws Exception {
        String reviewId = createReview("APPROVED", "我提交的审核");

        MvcResult result = mockMvc.perform(get("/api/admin/file-reviews/my-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].id").value(reviewId))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("content"));
    }

    /**
     * 字面量路径 /my-reviews 与模板路径 /{reviewId} 必须互不吞掉 ——
     * 契约里两条路径都在，靠测试锁住 Spring 的字面量优先规则。
     */
    @Test
    void myReviewsPathAndReviewIdPathDoNotSwallowEachOther() throws Exception {
        String reviewId = createReview("APPROVED", "路径互不吞掉");

        MvcResult mine = mockMvc.perform(get("/api/admin/file-reviews/my-reviews")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertNotNull(body(mine).get("content"), "/my-reviews 必须返回分页信封");

        MvcResult detail = mockMvc.perform(get("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andReturn();
        assertNotNull(body(detail).get("id"), "/{reviewId} 必须返回裸审核记录");
        assertFalse(body(detail).has("content"), "/{reviewId} 不能是分页信封");
    }

    @Test
    void myReviewsPageSizeIsClampedToASaneMaximum() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/my-reviews")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    // ------------------------------------------------------------ fileReviewGetByWorkId

    @Test
    void getReviewByWorkIdReturnsTheLatestReview() throws Exception {
        createReview("REJECTED", "第一次");
        String latest = createReview("APPROVED", "第二次");

        mockMvc.perform(get("/api/admin/file-reviews/work/" + workId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(latest))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                // 统计字段是「该作品」的汇总，不是这一条记录自身
                .andExpect(jsonPath("$.totalReviews").value(2))
                .andExpect(jsonPath("$.approvedCount").value(1))
                .andExpect(jsonPath("$.rejectedCount").value(1))
                .andExpect(jsonPath("$.revisionRequestedCount").value(0))
                .andExpect(jsonPath("$.approvalRate").value(0.5))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void getReviewByWorkIdWithoutAnyReviewReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/work/" + workId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void getReviewByWorkIdOnMissingWorkReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/work/987654321")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    // ------------------------------------------------------------ fileReviewGetById

    @Test
    void getReviewByIdReturnsBareResource() throws Exception {
        String reviewId = createReview("APPROVED", "详情");

        MvcResult result = mockMvc.perform(get("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertFalse(body(result).has("content"), "详情接口必须返回裸对象");
    }

    @Test
    void getReviewByIdOnMissingReviewReturns404() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ fileReviewUpdate

    @Test
    void updateReviewChangesTheConclusion() throws Exception {
        String reviewId = createReview("PENDING", "待定");

        mockMvc.perform(put("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "reviewStatus", "NEEDS_REVISION",
                                "reviewComments", "章法再紧一点"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId))
                .andExpect(jsonPath("$.status").value("NEEDS_REVISION"))
                .andExpect(jsonPath("$.reviewComment").value("章法再紧一点"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void updateMissingReviewReturns404() throws Exception {
        mockMvc.perform(put("/api/admin/file-reviews/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("reviewStatus", "APPROVED"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ fileReviewDelete

    @Test
    void deleteReviewReturns204AndRemovesTheRecord() throws Exception {
        String reviewId = createReview("APPROVED", "删掉我");

        mockMvc.perform(delete("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        assertTrue(fileReviewRepository.findById(reviewId).isEmpty(), "审核记录必须真的被删掉");
    }

    @Test
    void deleteMissingReviewReturns404() throws Exception {
        mockMvc.perform(delete("/api/admin/file-reviews/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    // ------------------------------------------------------------ 鉴权

    @Test
    void normalUserIsForbiddenFromAdminFileReviews() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/my-reviews")
                        .header("Authorization", "Bearer " + authorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());

        mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + authorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("workId", workId, "reviewStatus", "APPROVED"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousIsUnauthorizedForAdminFileReviews() throws Exception {
        mockMvc.perform(get("/api/admin/file-reviews/my-reviews"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").isNotEmpty());

        mockMvc.perform(post("/api/admin/file-reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("workId", workId, "reviewStatus", "APPROVED"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/admin/file-reviews/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isUnauthorized());
    }
}
