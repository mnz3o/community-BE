package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Integer>,
        CommentRepositoryCustom {

    // 삭제되지 않은 댓글 조회
    Optional<Comment> findByCommentIdAndDeletedAtIsNull(
            int commentId
    );
}