package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {

    // 게시글 검색
    List<PostListResponseDto> searchPosts(
            String keyword,
            Integer cursor,
            Pageable pageable
    );

    // 게시글 목록 조회
    List<PostListResponseDto> findPosts(
            Integer cursor,
            Pageable pageable
    );
}