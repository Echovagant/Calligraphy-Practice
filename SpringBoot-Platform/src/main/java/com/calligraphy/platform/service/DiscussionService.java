package com.calligraphy.platform.service;

import com.calligraphy.platform.dto.DiscussionDto;
import com.calligraphy.platform.dto.DiscussionCreateDto;
import com.calligraphy.platform.dto.DiscussionUpdateDto;
import com.calligraphy.platform.dto.DiscussionReplyDto;
import com.calligraphy.platform.dto.DiscussionReplyCreateDto;
import com.calligraphy.platform.model.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiscussionService {
    
    DiscussionDto createDiscussion(String userId, DiscussionCreateDto createDto);
    
    DiscussionDto getDiscussionById(String discussionId);
    
    DiscussionDto getDiscussionByIdWithReplies(String discussionId, Pageable replyPageable);
    
    Page<DiscussionDto> getAllDiscussions(Pageable pageable);
    
    List<DiscussionDto> getAllDiscussions();
    
    Page<DiscussionDto> getDiscussionsByCategory(String category, Pageable pageable);
    
    List<DiscussionDto> getDiscussionsByCategory(String category);
    
    Page<DiscussionDto> getDiscussionsByUserId(String userId, Pageable pageable);
    
    List<DiscussionDto> getDiscussionsByUserId(String userId);
    
    Page<DiscussionDto> searchDiscussions(String keyword, Pageable pageable);
    
    List<DiscussionDto> searchDiscussions(String keyword);
    
    DiscussionDto updateDiscussion(String discussionId, String userId, DiscussionUpdateDto updateDto);
    
    void deleteDiscussion(String discussionId, String userId);
    
    void incrementViewCount(String discussionId);
    
    void incrementLikeCount(String discussionId);
    
    void decrementLikeCount(String discussionId);
    
    boolean isLikedByUser(String discussionId, String userId);
    
    void addLike(String discussionId, String userId);
    
    void removeLike(String discussionId, String userId);
    
    DiscussionReplyDto createReply(String discussionId, String userId, DiscussionReplyCreateDto createDto);
    
    DiscussionReplyDto updateReply(String replyId, String userId, DiscussionReplyCreateDto updateDto);
    
    void deleteReply(String replyId, String userId);
    
    List<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId);
    
    Page<DiscussionReplyDto> getRepliesByDiscussionId(String discussionId, Pageable pageable);
    
    List<DiscussionReplyDto> getRepliesByDiscussionIdAndParentId(String discussionId, String parentId);
    
    long getTotalDiscussionCount();
    
    long getDiscussionCountByCategory(String category);
    
    long getDiscussionCountByUserId(String userId);
    
    long getDiscussionCountByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    List<DiscussionDto> getTopLikedDiscussions(int limit);
    
    List<DiscussionDto> getTopViewedDiscussions(int limit);
    
    List<DiscussionDto> getTopRepliedDiscussions(int limit);
    
    List<DiscussionDto> getPinnedDiscussions();
    
    List<DiscussionDto> getFeaturedDiscussions();
    
    DiscussionDto pinDiscussion(String discussionId, String adminId);
    
    DiscussionDto unpinDiscussion(String discussionId, String adminId);
    
    DiscussionDto featureDiscussion(String discussionId, String adminId);
    
    DiscussionDto unfeatureDiscussion(String discussionId, String adminId);
}


