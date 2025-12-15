package com.calligraphy.platform.repository;

import com.calligraphy.platform.model.DiscussionReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionReplyRepository extends JpaRepository<DiscussionReply, Long> {
    
    Page<DiscussionReply> findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(Long discussionId, DiscussionReply.ReplyStatus status, Pageable pageable);
    
    List<DiscussionReply> findByDiscussionIdAndStatusNotOrderByCreatedAtAsc(Long discussionId, DiscussionReply.ReplyStatus status);
    
    Page<DiscussionReply> findByUserIdAndStatusNotOrderByCreatedAtDesc(String userId, DiscussionReply.ReplyStatus status, Pageable pageable);
    
    List<DiscussionReply> findByUserIdAndStatusNotOrderByCreatedAtDesc(String userId, DiscussionReply.ReplyStatus status);
    
    Page<DiscussionReply> findByParentIdAndStatusNotOrderByCreatedAtAsc(Long parentId, DiscussionReply.ReplyStatus status, Pageable pageable);
    
    List<DiscussionReply> findByParentIdAndStatusNotOrderByCreatedAtAsc(Long parentId, DiscussionReply.ReplyStatus status);
    
    List<DiscussionReply> findByDiscussionIdAndParentIdAndStatusNotOrderByCreatedAtAsc(Long discussionId, Long parentId, DiscussionReply.ReplyStatus status);
    
    long countByDiscussionIdAndStatusNot(Long discussionId, DiscussionReply.ReplyStatus status);
    
    long countByUserIdAndStatusNot(String userId, DiscussionReply.ReplyStatus status);
    
    long countByParentIdAndStatusNot(Long parentId, DiscussionReply.ReplyStatus status);
    
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.discussionId = :discussionId AND dr.status != :status ORDER BY dr.likeCount DESC, dr.createdAt ASC")
    List<DiscussionReply> findTopLikedReplies(@Param("discussionId") Long discussionId, @Param("status") DiscussionReply.ReplyStatus status, Pageable pageable);
    
    @Query("SELECT dr FROM DiscussionReply dr WHERE dr.status != :status AND dr.createdAt >= :startDate ORDER BY dr.createdAt DESC")
    List<DiscussionReply> findRecentReplies(@Param("startDate") java.time.LocalDateTime startDate, @Param("status") DiscussionReply.ReplyStatus status, Pageable pageable);
}


