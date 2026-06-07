package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Integer> {

    // 삭제 안 된 게시글의 댓글 조회
    List<Comment> findByPost_PostIdAndDeletedAtIsNull(
            int postId
    );

    // 삭제 안 된 댓글 조회
    Optional<Comment> findByCommentIdAndDeletedAtIsNull(
            int commentId
    );
}