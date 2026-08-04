package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.constant.PostSearchType;
import com.example._ayelcommunitybe.constant.PostSortType;
import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostRepositoryCustom {

    List<PostListResponseDto> searchPosts(
            String keyword,
            PostSearchType searchType,
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            Pageable pageable
    );

    // 게시글 목록 조회
    List<PostListResponseDto> findPosts(
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            Pageable pageable
    );
}