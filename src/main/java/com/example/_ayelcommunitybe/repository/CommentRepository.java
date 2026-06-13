package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentRepository
        extends JpaRepository<Comment, Integer> {

    // 댓글 목록 조회
    @Query("""
            select c
            from Comment c
            join fetch c.user
            where c.post.postId = :postId
              and c.deletedAt is null
              and (
                    :cursor is null
                    or c.commentId < :cursor
              )
            order by c.commentId desc
            """)
    List<Comment> findComments(
            @Param("postId") int postId,
            @Param("cursor") Integer cursor,
            Pageable pageable
    );

    // 삭제되지 않은 댓글 조회
    Optional<Comment> findByCommentIdAndDeletedAtIsNull(
            int commentId
    );
}