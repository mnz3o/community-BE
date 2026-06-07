package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.PostLike;
import com.example._ayelcommunitybe.entity.PostLikeId;
import com.example._ayelcommunitybe.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository
        extends JpaRepository<PostLike, PostLikeId> {

    // 사용자가 특정 게시글에 누른 좋아요 조회
    Optional<PostLike> findByUserAndPost(
            User user,
            Post post
    );
}