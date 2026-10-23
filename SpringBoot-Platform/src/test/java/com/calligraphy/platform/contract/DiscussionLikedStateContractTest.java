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
import org.hibernate.SessionFactory;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
 * 讨论的「当前用户是否已点赞」（{@code Discussion.liked}）契约测试。
 *
 * <p>以前服务端根本不返回这个字段，前端的「已赞」高亮只在当前会话内有效，
 * 一刷新就丢 —— 用户看不出哪条是自己赞过的。
 *
 * <p>两个要点：
 * <ul>
 *   <li>未登录必须是 {@code false}（不是缺字段、也不是 null）；</li>
 *   <li>填充点赞态必须**批量查** —— 一页 20 条不能发 20 条 SQL。</li>
 * </ul>
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class DiscussionLikedStateContractTest {

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

    @Autowired
    private SessionFactory sessionFactory;

    /** 点赞的人。 */
    private String likerId;
    private String likerToken;

    /** 另一个登录用户（没点过赞）。 */
    private String otherToken;

    /** 讨论作者。 */
    private String authorId;

    private final List<String> createdUserIds = new ArrayList<>();
    private final List<Long> createdDiscussionIds = new ArrayList<>();

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        authorId = saveUser("dl_author_" + suffix, "Passw0rd" + suffix + "a");
        String likerPassword = "Passw0rd" + suffix + "b";
        likerId = saveUser("dl_liker_" + suffix, likerPassword);
        likerToken = loginFront("dl_liker_" + suffix, likerPassword);
        String otherPassword = "Passw0rd" + suffix + "c";
        saveUser("dl_other_" + suffix, otherPassword);
        otherToken = loginFront("dl_other_" + suffix, otherPassword);
    }

    @AfterEach
    void tearDown() {
        for (Long id : createdDiscussionIds) {
            List<DiscussionReply> replies = discussionReplyRepository
                    .findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(
                            id, DiscussionReply.ReplyStatus.DELETED, PageRequest.of(0, 500))
                    .getContent();
            discussionReplyRepository.deleteAll(replies);
            discussionLikeRepository.deleteByDiscussionId(id);
            discussionRepository.deleteById(id);
        }
        createdDiscussionIds.clear();
        for (String id : createdUserIds) {
            userRepository.findById(id).ifPresent(u -> userRepository.deleteById(u.getId()));
        }
        createdUserIds.clear();
    }

    // ------------------------------------------------------------ 基本语义

    @Test
    void anonymousSeesLikedFalse() throws Exception {
        String discussionId = saveDiscussion("点赞态匿名可见", false, false);

        MvcResult result = mockMvc.perform(get("/api/discussions").param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = findById(body(result).get("content"), discussionId);
        assertNotNull(node, "刚建的讨论应当在列表里");
        assertFalse(node.get("liked").asBoolean(), "未登录时 liked 必须是 false");
    }

    @Test
    void likerSeesLikedTrueAndOthersSeeFalse() throws Exception {
        String discussionId = saveDiscussion("点赞态可见性", false, false);
        like(discussionId, likerToken);

        // 点赞的人看：true
        MvcResult liked = mockMvc.perform(get("/api/discussions")
                        .header("Authorization", "Bearer " + likerToken)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = findById(body(liked).get("content"), discussionId);
        assertNotNull(node, "刚建的讨论应当在列表里");
        assertTrue(node.get("liked").asBoolean(), "点过赞的人必须看到 liked=true");

        // 没点过赞的另一个登录用户看：false
        MvcResult notLiked = mockMvc.perform(get("/api/discussions")
                        .header("Authorization", "Bearer " + otherToken)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode otherNode = findById(body(notLiked).get("content"), discussionId);
        assertNotNull(otherNode, "刚建的讨论应当在列表里");
        assertFalse(otherNode.get("liked").asBoolean(), "没点过赞的用户必须看到 liked=false");
    }

    @Test
    void detailAlsoCarriesTheLikedFlag() throws Exception {
        String discussionId = saveDiscussion("点赞态详情", false, false);
        like(discussionId, likerToken);

        MvcResult result = mockMvc.perform(get("/api/discussions/" + discussionId)
                        .header("Authorization", "Bearer " + likerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andReturn();
        assertTrue(body(result).get("liked").asBoolean(), "详情也要带 liked");
    }

    @Test
    void hotFeaturedPinnedAndSearchAllCarryTheLikedFlag() throws Exception {
        String plainId = saveDiscussion("点赞态列表用", false, false);
        String pinnedId = saveDiscussion("点赞态置顶用", true, false);
        String featuredId = saveDiscussion("点赞态精华用", false, true);
        like(plainId, likerToken);
        like(pinnedId, likerToken);
        like(featuredId, likerToken);

        assertTrue(likedFlagOf(get("/api/discussions/hot"), plainId),
                "热门榜也要带 liked");
        assertTrue(likedFlagOf(get("/api/discussions/pinned"), pinnedId),
                "置顶列表也要带 liked");
        assertTrue(likedFlagOf(get("/api/discussions/featured"), featuredId),
                "精华列表也要带 liked");
        assertTrue(likedFlagOf(get("/api/discussions/search").param("keyword", "点赞态列表用"), plainId),
                "搜索结果也要带 liked");
    }

    @Test
    void likeIsRecordedInTheDatabaseForTheCurrentUser() throws Exception {
        String discussionId = saveDiscussion("点赞落库", false, false);
        like(discussionId, likerToken);

        assertTrue(discussionLikeRepository
                        .findByDiscussionIdAndUserId(Long.valueOf(discussionId), likerId)
                        .isPresent(),
                "点赞必须落库（discussion_likes 里应有该用户对该讨论的记录）");
    }

    // ------------------------------------------------------------ 批量查，不能 N+1

    /**
     * 一页 12 条讨论时，点赞态必须是**一次** IN 查询查出来的，
     * 不能一条一条查（那是又造一个 N+1）。
     *
     * <p>用 Hibernate 的「预处理语句计数」量：批量查时语句数与讨论条数无关，
     * 逐条查时会随条数线性增长。
     */
    @Test
    void likedFlagsAreFetchedInOneBatchQuery() throws Exception {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            ids.add(saveDiscussion("批量点赞态 " + i, false, false));
        }
        like(ids.get(0), likerToken);
        like(ids.get(5), likerToken);

        sessionFactory.getStatistics().clear();
        mockMvc.perform(get("/api/discussions")
                        .header("Authorization", "Bearer " + likerToken)
                        .param("size", "50"))
                .andExpect(status().isOk());
        long statements = sessionFactory.getStatistics().getPrepareStatementCount();

        // 12 条讨论：批量查 = 一条 IN 查询；逐条查 = 12 条。
        // 留出余量给认证、列表、计数等固定语句。
        assertTrue(statements <= 10L,
                "点赞态应当一次批量查完，实际执行了 " + statements + " 条语句（逐条查会超过 10 条）");
    }

    // ------------------------------------------------------------ 工具

    private boolean likedFlagOf(MockHttpServletRequestBuilder builder, String discussionId) throws Exception {
        MvcResult result = mockMvc.perform(builder
                        .header("Authorization", "Bearer " + likerToken)
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = findById(body(result).get("content"), discussionId);
        assertNotNull(node, "讨论 " + discussionId + " 应当出现在结果里");
        return node.get("liked").asBoolean();
    }

    private void like(String discussionId, String token) throws Exception {
        mockMvc.perform(post("/api/discussions/" + discussionId + "/like")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(true))
                .andExpect(jsonPath("$.likeCount").value(1));
    }

    private String saveUser(String username, String password) {
        String id = userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .password(passwordEncoder.encode(password))
                .nickname("点赞态测试")
                .status(User.UserStatus.ACTIVE)
                .emailVerified(true)
                .twoFactorEnabled(false)
                .loginCount(0L)
                .role(User.Role.USER)
                .build()).getId();
        createdUserIds.add(id);
        return id;
    }

    private String loginFront(String username, String password) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/frontend/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();
        return body(login).get("accessToken").asText();
    }

    /** 直接落库建一条讨论，返回它的 id（字符串形式）。 */
    private String saveDiscussion(String title, boolean pinned, boolean featured) {
        User author = userRepository.findById(authorId).orElseThrow();
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
        createdDiscussionIds.add(id);
        return String.valueOf(id);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * 读响应体的**原始字节**。
     *
     * <p>MockHttpServletResponse 的 characterEncoding 默认是 ISO-8859-1，
     * getContentAsString() 会把 UTF-8 中文读成乱码 —— 中文断言会莫名其妙地失败。
     */
    private JsonNode body(MvcResult result) throws Exception {
        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 0, "响应体不能为空");
        JsonNode node = objectMapper.readTree(content);
        assertNotNull(node);
        return node;
    }

    private JsonNode findById(JsonNode content, String id) {
        if (content == null || !content.isArray()) {
            return null;
        }
        for (JsonNode node : content) {
            JsonNode nodeId = node.get("id");
            if (nodeId != null && id.equals(nodeId.asText())) {
                return node;
            }
        }
        return null;
    }
}
