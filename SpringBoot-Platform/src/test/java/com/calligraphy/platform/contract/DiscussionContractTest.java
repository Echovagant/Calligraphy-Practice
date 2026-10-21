package com.calligraphy.platform.contract;

import com.calligraphy.platform.model.Discussion;
import com.calligraphy.platform.model.DiscussionReply;
import com.calligraphy.platform.model.User;
import com.calligraphy.platform.repository.DiscussionLikeRepository;
import com.calligraphy.platform.repository.DiscussionReplyRepository;
import com.calligraphy.platform.repository.DiscussionRepository;
import com.calligraphy.platform.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
 * discussions 域的契约测试（P5）。
 *
 * <p>14 个 operation 各有一个测试方法，另加错误路径与「字面量路径 / 模板路径
 * 互不吞掉」的断言。刻意**只依赖契约**（method / path / 状态码 / 字段名），
 * 不引用实现层的 DTO 类。
 *
 * <p>主键字段一律叫 {@code id}（不是 discussionId / replyId），
 * 成功响应里没有 {@code data} 信封。
 *
 * <p>测试造出来的数据必须成对清理：先删回复与点赞，再删帖子，最后删用户
 * —— discussions.user_id 上有指向 users 的外键，顺序反了会删不掉。
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DiscussionContractTest {

    private static final long MISSING_ID = 999_999_999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private DiscussionReplyRepository discussionReplyRepository;

    @Autowired
    private DiscussionLikeRepository discussionLikeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 作者（帖子主人）。 */
    private String ownerId;
    private String ownerUsername;
    private String ownerToken;

    /** 另一个普通用户，用来验证越权（403）。 */
    private String otherId;
    private String otherToken;

    /** @BeforeEach 里用 API 建的帖子。 */
    private String discussionId;

    private final List<String> createdUserIds = new ArrayList<>();
    private final List<String> createdDiscussionIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        ownerUsername = newUsername("ds_owner");
        ownerToken = createUser(ownerUsername, User.Role.USER);
        ownerId = createdUserIds.get(createdUserIds.size() - 1);

        String otherUsername = newUsername("ds_other");
        otherToken = createUser(otherUsername, User.Role.USER);
        otherId = createdUserIds.get(createdUserIds.size() - 1);

        // 用 API 建帖子：字段与真实链路一致（作者、状态、计数都由后端填）
        MvcResult created = mockMvc.perform(post("/api/discussions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody("契约测试讨论", "讨论正文", "测试分类", "标签A,标签B"))))
                .andExpect(status().isCreated())
                .andReturn();
        discussionId = body(created).get("id").asText();
        createdDiscussionIds.add(discussionId);
    }

    @AfterEach
    void tearDown() {
        for (String id : createdDiscussionIds) {
            Long discussionKey = Long.valueOf(id);
            // 回复要先删：discussion_replies.discussion_id 上有外键
            List<DiscussionReply> replies = discussionReplyRepository
                    .findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                            discussionKey, DiscussionReply.ReplyStatus.DELETED, PageRequest.of(0, 500))
                    .getContent();
            discussionReplyRepository.deleteAll(replies);
            discussionLikeRepository.deleteByDiscussionId(discussionKey);
            discussionRepository.deleteById(discussionKey);
        }
        createdDiscussionIds.clear();
        // 用户最后删：discussions.user_id 上有指向 users 的外键
        for (String id : createdUserIds) {
            userRepository.findById(id).ifPresent(user -> userRepository.deleteById(user.getId()));
        }
        createdUserIds.clear();
    }

    // ------------------------------------------------------------------
    // 6 个公开读端点
    // ------------------------------------------------------------------

    @Test
    void discussionListIsPublicAndPaged() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/discussions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        // 刚建的帖子必须在列表里
        assertTrue(idsIn(body(result).get("content")).contains(discussionId),
                "列表里必须能看到刚发的帖子");
    }

    @Test
    void discussionListFiltersByCategoryAndKeyword() throws Exception {
        mockMvc.perform(get("/api/discussions").param("category", "测试分类"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].category").value("测试分类"));

        MvcResult result = mockMvc.perform(get("/api/discussions").param("keyword", "契约测试讨论"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertTrue(idsIn(body(result).get("content")).contains(discussionId),
                "按关键词搜索必须能搜到刚发的帖子");
    }

    @Test
    void discussionListAcceptsSortParameter() throws Exception {
        mockMvc.perform(get("/api/discussions").param("sort", "mostLiked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mockMvc.perform(get("/api/discussions").param("sort", "hot"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        // 认不出来的排序值按默认处理，不能 500
        mockMvc.perform(get("/api/discussions").param("sort", "乱七八糟"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void discussionHotIsPublicAndPaged() throws Exception {
        mockMvc.perform(get("/api/discussions/hot").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void discussionFeaturedIsPublicAndPaged() throws Exception {
        mockMvc.perform(get("/api/discussions/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void discussionPinnedIsPublicAndPaged() throws Exception {
        mockMvc.perform(get("/api/discussions/pinned"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void discussionSearchIsPublicAndFindsByKeyword() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/discussions/search").param("keyword", "契约测试讨论"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").isNumber())
                .andExpect(jsonPath("$.total").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertTrue(idsIn(body(result).get("content")).contains(discussionId),
                "搜索必须能搜到刚发的帖子");
    }

    @Test
    void discussionGetByIdIsPublicAndReturnsBareObject() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/discussions/" + discussionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discussionId))
                .andExpect(jsonPath("$.title").value("契约测试讨论"))
                .andExpect(jsonPath("$.content").value("讨论正文"))
                .andExpect(jsonPath("$.category").value("测试分类"))
                .andExpect(jsonPath("$.userId").value(ownerId))
                .andExpect(jsonPath("$.username").value(ownerUsername))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.viewCount").isNumber())
                .andExpect(jsonPath("$.likeCount").isNumber())
                .andExpect(jsonPath("$.replyCount").isNumber())
                .andExpect(jsonPath("$.isPinned").isBoolean())
                .andExpect(jsonPath("$.isFeatured").isBoolean())
                .andExpect(jsonPath("$.tags").value("标签A,标签B"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.replies").isArray())
                // 主键叫 id，不叫 discussionId
                .andExpect(jsonPath("$.discussionId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("id"));
    }

    /**
     * 字面量路径（/hot、/featured、/pinned、/search）与模板路径（/{id}）必须互不吞掉 ——
     * 契约里明确写了这条，Spring 的字面量优先规则要靠测试锁住。
     */
    @Test
    void literalPathsAndTemplatePathDoNotSwallowEachOther() throws Exception {
        for (String literal : List.of("/hot", "/featured", "/pinned")) {
            MvcResult page = mockMvc.perform(get("/api/discussions" + literal))
                    .andExpect(status().isOk())
                    .andReturn();
            JsonNode pageBody = body(page);
            assertNotNull(pageBody.get("content"), literal + " 必须返回分页信封");
            assertTrue(pageBody.get("content").isArray(), literal + " 的 content 必须是数组");
            assertTrue(pageBody.has("total"), literal + " 必须带分页计数");
        }

        // /search 也是字面量路径，keyword 是必填参数
        MvcResult search = mockMvc.perform(get("/api/discussions/search").param("keyword", "契约测试讨论"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(body(search).get("content"), "/search 必须返回分页信封");

        MvcResult detail = mockMvc.perform(get("/api/discussions/" + discussionId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode detailBody = body(detail);
        assertNotNull(detailBody.get("id"), "/{id} 必须返回裸讨论");
        // 裸讨论没有分页信封的字段（注意：讨论自己的 content 是正文，不能拿来当判据）
        assertFalse(detailBody.has("total"), "/{id} 不能返回分页信封");
        assertFalse(detailBody.has("totalPages"), "/{id} 不能返回分页信封");
        assertFalse(detailBody.has("page"), "/{id} 不能返回分页信封");
    }

    @Test
    void detailIncrementsViewCount() throws Exception {
        int before = discussionRepository.findById(Long.valueOf(discussionId)).orElseThrow().getViewCount();
        mockMvc.perform(get("/api/discussions/" + discussionId)).andExpect(status().isOk());
        int after = discussionRepository.findById(Long.valueOf(discussionId)).orElseThrow().getViewCount();
        assertEquals(before + 1, after, "看一次详情，浏览数应 +1");
    }

    // ------------------------------------------------------------------
    // 4 个写端点（帖子）
    // ------------------------------------------------------------------

    @Test
    void discussionCreateReturns201WithContractFields() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/discussions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody("新帖子", "正文", "学习交流", "标签1,标签2"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("新帖子"))
                .andExpect(jsonPath("$.content").value("正文"))
                .andExpect(jsonPath("$.category").value("学习交流"))
                .andExpect(jsonPath("$.userId").value(ownerId))
                .andExpect(jsonPath("$.username").value(ownerUsername))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.viewCount").value(0))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.replyCount").value(0))
                .andExpect(jsonPath("$.isPinned").value(false))
                .andExpect(jsonPath("$.isFeatured").value(false))
                .andExpect(jsonPath("$.tags").value("标签1,标签2"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.replies").isArray())
                .andExpect(jsonPath("$.discussionId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        createdDiscussionIds.add(body(result).get("id").asText());
    }

    @Test
    void discussionCreateWithoutCategoryFallsBackToDefault() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "没写分类");
        payload.put("content", "正文");

        MvcResult result = mockMvc.perform(post("/api/discussions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").isNotEmpty())
                .andReturn();
        createdDiscussionIds.add(body(result).get("id").asText());
    }

    @Test
    void discussionUpdateReturns200AndChangesFields() throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", "改过的标题");
        payload.put("content", "改过的正文");
        payload.put("tags", "新标签");

        MvcResult result = mockMvc.perform(put("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discussionId))
                .andExpect(jsonPath("$.title").value("改过的标题"))
                .andExpect(jsonPath("$.content").value("改过的正文"))
                .andExpect(jsonPath("$.tags").value("新标签"))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("id"));
    }

    @Test
    void discussionDeleteReturns204AndThenDisappears() throws Exception {
        mockMvc.perform(delete("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/discussions/" + discussionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void discussionLikeIsBoundedByDistinctUsers() throws Exception {
        // 同一个用户反复点赞，计数不会越过 1
        mockMvc.perform(post("/api/discussions/" + discussionId + "/like")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.data").doesNotExist());

        // 再点一次 = 取消点赞，计数回到 0（没有「加两次」）
        mockMvc.perform(post("/api/discussions/" + discussionId + "/like")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(false))
                .andExpect(jsonPath("$.likeCount").value(0));

        // 另一个用户点赞，计数 +1
        mockMvc.perform(post("/api/discussions/" + discussionId + "/like")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    // ------------------------------------------------------------------
    // 4 个回复端点
    // ------------------------------------------------------------------

    @Test
    void discussionReplyListIsPublicAndPaged() throws Exception {
        createReply("第一条回复");

        MvcResult result = mockMvc.perform(get("/api/discussions/" + discussionId + "/replies")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("content"));
    }

    @Test
    void discussionReplyCreateReturns201WithContractFields() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/discussions/" + discussionId + "/replies")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "这是一条回复"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.content").value("这是一条回复"))
                .andExpect(jsonPath("$.discussionId").value(discussionId))
                .andExpect(jsonPath("$.userId").value(ownerId))
                .andExpect(jsonPath("$.username").value(ownerUsername))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                // 主键叫 id，不叫 replyId
                .andExpect(jsonPath("$.replyId").doesNotExist())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("id"));
    }

    @Test
    void discussionReplyUpdateReturns200AndChangesContent() throws Exception {
        String replyId = createReply("改之前的回复");

        MvcResult result = mockMvc.perform(put("/api/discussions/replies/" + replyId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "改之后的回复"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(replyId))
                .andExpect(jsonPath("$.content").value("改之后的回复"))
                .andExpect(jsonPath("$.discussionId").value(discussionId))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andReturn();
        assertNotNull(body(result).get("id"));
    }

    @Test
    void discussionReplyDeleteReturns204AndThenDisappears() throws Exception {
        String replyId = createReply("待删除的回复");

        mockMvc.perform(delete("/api/discussions/replies/" + replyId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/discussions/" + discussionId + "/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    /**
     * 两段路径的 PUT（/discussions/replies/{replyId}）与一段路径的 PUT（/discussions/{id}）
     * 不能互相吞掉。
     */
    @Test
    void replyUpdatePathAndDiscussionUpdatePathDoNotCollide() throws Exception {
        String replyId = createReply("路径不打架");

        mockMvc.perform(put("/api/discussions/replies/" + replyId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "改过的回复"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(replyId));

        mockMvc.perform(put("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "帖子标题没串"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(discussionId))
                .andExpect(jsonPath("$.title").value("帖子标题没串"))
                // 帖子自己的 content 字段（正文）还在，只是没被回复的字段串味
                .andExpect(jsonPath("$.content").value("讨论正文"));
    }

    // ------------------------------------------------------------------
    // 错误路径
    // ------------------------------------------------------------------

    @Test
    void anonymousDiscussionWritesAreRejected() throws Exception {
        mockMvc.perform(post("/api/discussions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(createBody("匿名发帖", "正文", "学习交流", null))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/discussions/" + discussionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "匿名改帖"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/discussions/" + discussionId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/discussions/" + discussionId + "/like"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/discussions/" + discussionId + "/replies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "匿名回复"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void replyingToMissingDiscussionReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/discussions/" + MISSING_ID + "/replies")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "给不存在的帖子回复"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void updatingSomeoneElsesDiscussionIsForbidden() throws Exception {
        mockMvc.perform(put("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "我要改别人的帖子"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void deletingSomeoneElsesDiscussionIsForbidden() throws Exception {
        mockMvc.perform(delete("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void updatingSomeoneElsesReplyIsForbidden() throws Exception {
        String replyId = createReply("别人的回复");

        mockMvc.perform(put("/api/discussions/replies/" + replyId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "我要改别人的回复"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void deletingSomeoneElsesReplyIsForbidden() throws Exception {
        String replyId = createReply("别人的回复");

        mockMvc.perform(delete("/api/discussions/replies/" + replyId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void deletingMissingDiscussionReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/discussions/" + MISSING_ID)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void likingMissingDiscussionReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/discussions/" + MISSING_ID + "/like")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void gettingMissingDiscussionReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/discussions/" + MISSING_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void updatingMissingDiscussionReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/discussions/" + MISSING_ID)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "改不存在的帖子"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void updatingMissingReplyReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/discussions/replies/" + MISSING_ID)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "改不存在的回复"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void deletingMissingReplyReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/discussions/replies/" + MISSING_ID)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test
    void creatingDiscussionWithoutTitleIsRejected() throws Exception {
        mockMvc.perform(post("/api/discussions")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", "只有正文没有标题"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void listPageSizeIsClampedToASaneMaximum() throws Exception {
        mockMvc.perform(get("/api/discussions").param("size", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
        mockMvc.perform(get("/api/discussions").param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void pinnedAndFeaturedEndpointsReturnTheFlaggedDiscussions() throws Exception {
        // 置顶 / 精华没有对应的管理端点（契约里没有），这里直接落库来验证查询是真的
        String pinnedId = saveDiscussionWithFlags("被置顶的讨论", true, false);
        String featuredId = saveDiscussionWithFlags("被加精的讨论", false, true);

        MvcResult pinned = mockMvc.perform(get("/api/discussions/pinned"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(idsIn(body(pinned).get("content")).contains(pinnedId),
                "置顶列表必须包含 isPinned=true 的讨论");

        MvcResult featured = mockMvc.perform(get("/api/discussions/featured"))
                .andExpect(status().isOk())
                .andReturn();
        assertTrue(idsIn(body(featured).get("content")).contains(featuredId),
                "精华列表必须包含 isFeatured=true 的讨论");
    }

    // ------------------------------------------------------------------
    // 内部工具
    // ------------------------------------------------------------------

    private String newUsername(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /** 建用户 + 走真实登录端点拿令牌，返回 accessToken。 */
    private String createUser(String username, User.Role role) throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String password = "Passw0rd" + suffix;
        String userId = userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("契约测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(role)
                .build()).getId();
        createdUserIds.add(userId);

        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        String token = body(login).get("accessToken").asText();
        assertFalse(token.isBlank(), "登录必须拿到 accessToken");
        return token;
    }

    /** 直接落库建一条带置顶 / 精华标记的讨论。 */
    private String saveDiscussionWithFlags(String title, boolean pinned, boolean featured) {
        User author = userRepository.findById(ownerId).orElseThrow();
        Discussion discussion = Discussion.builder()
                .title(title)
                .content("正文")
                .category("测试分类")
                .user(author)
                .userId(author.getId())
                .username(author.getUsername())
                .status(Discussion.DiscussionStatus.ACTIVE)
                .viewCount(0)
                .likeCount(0)
                .replyCount(0)
                .isPinned(pinned)
                .isFeatured(featured)
                .build();
        Long id = discussionRepository.save(discussion).getId();
        createdDiscussionIds.add(String.valueOf(id));
        return String.valueOf(id);
    }

    /** 建一条回复，返回它的 id。 */
    private String createReply(String content) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/discussions/" + discussionId + "/replies")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("content", content))))
                .andExpect(status().isCreated())
                .andReturn();
        return body(result).get("id").asText();
    }

    private Map<String, Object> createBody(String title, String content, String category, String tags) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("content", content);
        if (category != null) {
            payload.put("category", category);
        }
        if (tags != null) {
            payload.put("tags", tags);
        }
        return payload;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private JsonNode body(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        assertFalse(content.isBlank(), "响应体不能为空");
        return objectMapper.readTree(content);
    }

    /** 取出 content 数组里的所有 id。 */
    private List<String> idsIn(JsonNode content) {
        List<String> ids = new ArrayList<>();
        if (content == null || !content.isArray()) {
            return ids;
        }
        for (JsonNode node : content) {
            JsonNode id = node.get("id");
            if (id != null) {
                ids.add(id.asText());
            }
        }
        return ids;
    }
}
