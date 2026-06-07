package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository
        extends JpaRepository<Post, Integer> {

    // 게시글 조회 시 작성자(User)도 함께 조회
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

    // 삭제 안 된 게시글 조회
    Optional<Post> findByPostIdAndDeletedAtIsNull(
            int postId
    );
}