package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.post.*;
import com.example._ayelcommunitybe.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<ApiResponse<PostCreateResponseDto>> createPost(
            @RequestAttribute(SessionConst.USER_ID) int userId,
            @Valid @RequestBody PostCreateRequestDto request
    ) {

        int postId = postService.createPost(userId, request);

        return ResponseEntity
                .created(URI.create("/posts/" + postId))
                .body(
                        ApiResponse.success(
                                "게시글 작성 성공",
                                new PostCreateResponseDto(postId))
                );
    }

    // 게시글 목록 조회
    @GetMapping
    public ApiResponse<PostPageResponseDto> getPosts(
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ApiResponse.success(
                "게시글 목록 조회 성공",
                postService.getPosts(
                        cursor,
                        limit
                )
        );
    }

    // 게시글 검색
    @GetMapping("/search")
    public ApiResponse<PostPageResponseDto> searchPosts(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer cursor,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ApiResponse.success(
                "게시글 검색 성공",
                postService.searchPosts(
                        keyword,
                        cursor,
                        limit
                )
        );
    }

    // 게시글 상세 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDto> getPost(
            @PathVariable int postId
    ) {

        return ApiResponse.success(
                "게시글 상세 조회 성공",
                postService.getPost(postId)
        );
    }

    // 게시글 수정
    @PatchMapping("/{postId}")
    public ApiResponse<Void> updatePost(
            @PathVariable int postId,
            @RequestAttribute(SessionConst.USER_ID) int userId,
            @Valid @RequestBody PostUpdateRequestDto request
    ) {

        postService.updatePost(
                userId,
                postId,
                request
        );

        return ApiResponse.success(
                "게시글 수정 성공"
        );
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable int postId,
            @RequestAttribute(SessionConst.USER_ID) int userId
    ) {

        postService.deletePost(
                userId,
                postId
        );

        return ApiResponse.success(
                "게시글 삭제 성공"
        );
    }
}