package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository
        extends JpaRepository<Post, Integer>,
        PostRepositoryCustom {

    // 조회수 증가
    @Modifying
    @Query("""
        update Post p
        set p.viewCount = p.viewCount + 1
        where p.postId = :postId
        """)
    int increaseViewCount(
            @Param("postId") int postId
    );

    // 게시글 목록 조회
    @Query("""
        select p
        from Post p
        join fetch p.user
        where p.deletedAt is null
          and (
                :cursor is null
                or p.postId < :cursor
          )
        order by p.postId desc
        """)
    List<Post> findPosts(
            @Param("cursor") Integer cursor,
            Pageable pageable
    );

    // 게시글 상세 조회
    @Query("""
            select distinct p
            from Post p
            join fetch p.user
            left join fetch p.files
            where p.postId = :postId
              and p.deletedAt is null
            """)
    Optional<Post> findPostDetail(
            @Param("postId") int postId
    );

    // 좋아요 증가
    @Modifying
    @Query("""
    update Post p
    set p.likeCount = p.likeCount + 1
    where p.postId = :postId
    """)
    int increaseLikeCount(
            @Param("postId") int postId
    );

    // 좋아요 감소
    @Modifying
    @Query("""
    update Post p
    set p.likeCount =
        case
            when p.likeCount > 0
            then p.likeCount - 1
            else 0
        end
    where p.postId = :postId
    """)
    int decreaseLikeCount(
            @Param("postId") int postId
    );

    // 댓글 증가
    @Modifying
    @Query("""
    update Post p
    set p.commentCount = p.commentCount + 1
    where p.postId = :postId
    """)
    int increaseCommentCount(
            @Param("postId") int postId
    );

    // 댓글 감소
    @Modifying
    @Query("""
    update Post p
    set p.commentCount =
        case
            when p.commentCount > 0
            then p.commentCount - 1
            else 0
        end
    where p.postId = :postId
    """)
    int decreaseCommentCount(
            @Param("postId") int postId
    );

    // 삭제되지 않은 게시글 조회
    Optional<Post> findByPostIdAndDeletedAtIsNull(
            int postId
    );
}