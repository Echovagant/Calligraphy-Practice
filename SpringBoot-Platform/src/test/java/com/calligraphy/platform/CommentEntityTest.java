package com.calligraphy.platform;

import com.calligraphy.platform.model.Comment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommentEntityTest {
    
    @Test
    public void testCommentStatusEnum() {
        // Test that all status values are available
        assertNotNull(Comment.CommentStatus.PENDING);
        assertNotNull(Comment.CommentStatus.APPROVED);
        assertNotNull(Comment.CommentStatus.REJECTED);
        assertNotNull(Comment.CommentStatus.DELETED);
        
        // Test setting status
        Comment comment = new Comment();
        comment.setStatus(Comment.CommentStatus.PENDING);
        assertEquals(Comment.CommentStatus.PENDING, comment.getStatus());
        
        comment.setStatus(Comment.CommentStatus.DELETED);
        assertEquals(Comment.CommentStatus.DELETED, comment.getStatus());
    }
}