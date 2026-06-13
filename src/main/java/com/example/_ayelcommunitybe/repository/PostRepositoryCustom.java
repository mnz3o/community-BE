package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Post;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {

    // 게시글 검색
    List<Post> searchPosts(
            String keyword,
            Integer cursor,
            Pageable pageable
    );
}