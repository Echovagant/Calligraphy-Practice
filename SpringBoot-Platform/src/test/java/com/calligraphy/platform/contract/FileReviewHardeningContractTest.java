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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 作品审核的边界测试。
 *
 * <ul>
 *   <li>批量审核必须校验作品状态：已审过的作品再审一次会覆盖上一次的结论
 *       （以前直接就审了）；</li>
 *   <li>删除审核记录要判归属：不是本人的记录删不得（以前谁都能删）；</li>
 *   <li>删除审核记录后，关联作品的 review_status 必须回滚到未审状态。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class FileReviewHardeningContractTest {

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

    private String authorId;

    /** 审核人（ADMIN）—— 提交审核记录的人。 */
    private String reviewerUsername;
    private String reviewerPassword;
    private String reviewerToken;

    /** 另一个管理员（ADMIN，不是 SUPER_ADMIN）—— 用来验证「不是本人的记录删不得」。 */
    private String otherAdminUsername;
    private String otherAdminPassword;
    private String otherAdminToken;

    /** 超级管理员 —— 允许删别人的审核记录。 */
    private String superAdminUsername;
    private String superAdminPassword;
    private String superAdminToken;

    private Long workId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String authorPassword = "Passw0rd" + suffix;
        authorId = userRepository.save(User.builder()
                .username("frh_author_" + suffix)
                .email("frh_author_" + suffix + "@example.com")
                .password(passwordEncoder.encode(authorPassword))
                .nickname("普通用户")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();

        reviewerPassword = "Passw0rd" + suffix;
        reviewerUsername = "frh_reviewer_" + suffix;
        createUser(reviewerUsername, "frh_reviewer_" + suffix + "@example.com", reviewerPassword, User.Role.ADMIN);
        reviewerToken = loginAdmin(reviewerUsername, reviewerPassword);

        otherAdminPassword = "Passw0rd" + suffix;
        otherAdminUsername = "frh_other_" + suffix;
        createUser(otherAdminUsername, "frh_other_" + suffix + "@example.com", otherAdminPassword, User.Role.ADMIN);
        otherAdminToken = loginAdmin(otherAdminUsername, otherAdminPassword);

        superAdminPassword = "Passw0rd" + suffix;
        superAdminUsername = "frh_super_" + suffix;
        createUser(superAdminUsername, "frh_super_" + suffix + "@example.com", superAdminPassword, User.Role.SUPER_ADMIN);
        superAdminToken = loginAdmin(superAdminUsername, superAdminPassword);

        User author = userRepository.findById(authorId).orElseThrow();
        workId = workRepository.save(Work.builder()
                .title("待审作品")
                .description("描述")
                .content("内容")
                .imageUrl("/api/files/frh-contract-test.png")
                .user(author)
                .userId(authorId)
                .status(Work.WorkStatus.DRAFT)
                .reviewStatus(Work.ReviewStatus.PENDING)
                .likeCount(0)
                .commentCount(0)
                .viewCount(0)
                .isFeatured(false)
                .build()).getId();
    }

    @AfterEach
    void tearDown() {
        // file_reviews.work_id 指向 works，审核记录必须先删
        fileReviewRepository.findByWorkId(workId).forEach(review -> fileReviewRepository.delete(review));
        workRepository.findById(workId).ifPresent(work -> workRepository.deleteById(work.getId()));

        // backend_users 里给管理员补建的审核员记录也要清掉
        for (String name : List.of(reviewerUsername, otherAdminUsername, superAdminUsername)) {
            backendUserRepository.findByUsername(name)
                    .ifPresent(reviewer -> backendUserRepository.delete(reviewer));
        }

        userRepository.findByUsername(reviewerUsername).ifPresent(user -> userRepository.deleteById(user.getId()));
        userRepository.findByUsername(otherAdminUsername).ifPresent(user -> userRepository.deleteById(user.getId()));
        userRepository.findByUsername(superAdminUsername).ifPresent(user -> userRepository.deleteById(user.getId()));
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
    }

    // ------------------------------------------------------------ 批量审核要校验状态

    @Test
    void batchReviewRejectsAWorkThatWasAlreadyReviewed() throws Exception {
        // 先正常审一次：作品变成 APPROVED
        mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", workId,
                                "reviewStatus", "APPROVED",
                                "reviewComments", "第一次"))))
                .andExpect(status().isCreated());

        // 再批量审一次：必须计入失败，不能覆盖掉上一次的结论
        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews/batch")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workIds", List.of(workId),
                                "reviewStatus", "REJECTED",
                                "reviewComments", "批量改判"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.succeeded").value(0))
                .andExpect(jsonPath("$.failed").value(1))
                .andExpect(jsonPath("$.reviews.length()").value(0))
                .andReturn();
        assertNotNull(body(result));

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.APPROVED, work.getReviewStatus(),
                "已审过的作品不能被批量审核改判");
        assertEquals(1, fileReviewRepository.findByWorkId(workId).size(),
                "已审过的作品不该多出一条审核记录");
    }

    @Test
    void batchReviewStillAcceptsAWorkThatCanBeReviewed() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews/batch")
                        .header("Authorization", "Bearer " + reviewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workIds", List.of(workId),
                                "reviewStatus", "APPROVED",
                                "reviewComments", "批量通过"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.succeeded").value(1))
                .andExpect(jsonPath("$.failed").value(0))
                .andReturn();
        assertNotNull(body(result));

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.APPROVED, work.getReviewStatus(), "批量审核成功必须回写作品状态");
    }

    // ------------------------------------------------------------ 删除审核记录要判归属

    @Test
    void deletingSomebodyElsesReviewIsForbidden() throws Exception {
        String reviewId = createReview(reviewerToken, "APPROVED", "我审的");

        mockMvc.perform(delete("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + otherAdminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());

        assertTrue(fileReviewRepository.findById(reviewId).isPresent(), "越权删除后审核记录必须还在");
    }

    @Test
    void theAuthorOfTheReviewCanDeleteIt() throws Exception {
        String reviewId = createReview(reviewerToken, "APPROVED", "我审的");

        mockMvc.perform(delete("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + reviewerToken))
                .andExpect(status().isNoContent());

        assertTrue(fileReviewRepository.findById(reviewId).isEmpty(), "审核记录必须真的被删掉");
    }

    @Test
    void superAdminCanDeleteSomebodyElsesReview() throws Exception {
        String reviewId = createReview(reviewerToken, "APPROVED", "别人审的");

        mockMvc.perform(delete("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isNoContent());

        assertTrue(fileReviewRepository.findById(reviewId).isEmpty(), "审核记录必须真的被删掉");
    }

    // ------------------------------------------------------------ 删除后回滚作品状态

    @Test
    void deletingTheLastReviewRollsBackTheWorkReviewStatus() throws Exception {
        String reviewId = createReview(reviewerToken, "APPROVED", "通过");

        Work approved = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.APPROVED, approved.getReviewStatus(), "前置条件：审核结论已回写");

        mockMvc.perform(delete("/api/admin/file-reviews/" + reviewId)
                        .header("Authorization", "Bearer " + reviewerToken))
                .andExpect(status().isNoContent());

        Work work = workRepository.findById(workId).orElseThrow();
        assertEquals(Work.ReviewStatus.PENDING, work.getReviewStatus(),
                "审核记录删了，作品的 review_status 必须回滚到未审状态");
        assertEquals(Work.WorkStatus.DRAFT, work.getStatus(),
                "审核记录删了，作品不能还留在已发布状态");
    }

    // ------------------------------------------------------------ 工具

    private String createReview(String token, String reviewStatus, String comments) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/file-reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "workId", workId,
                                "reviewStatus", reviewStatus,
                                "reviewComments", comments))))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).get("id").asText();
    }

    private String createUser(String username, String email, String rawPassword, User.Role role) {
        return userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(username)
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(role)
                .build()).getId();
    }

    private String loginAdmin(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/backend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
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
