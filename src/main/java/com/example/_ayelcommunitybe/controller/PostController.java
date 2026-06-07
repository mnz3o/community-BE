package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.post.*;
import com.example._ayelcommunitybe.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<PostCreateResponseDto> createPost(
            @Valid @RequestBody PostCreateRequestDto request,
            HttpServletRequest httpRequest
    ) {

        HttpSession session = httpRequest.getSession(false);

        int userId = (int) session.getAttribute("user_id");

        int postId = postService.createPost(
                userId,
                request
        );

        return ApiResponse.success(
                "게시글 작성 성공",
                new PostCreateResponseDto(postId)
        );
    }

    // 게시글 목록 조회
    @GetMapping
    public ApiResponse<PostPageResponseDto> getPosts(
            @RequestParam(required = false)
            Integer cursor,

            @RequestParam(defaultValue = "10")
            int limit
    ) {

        return ApiResponse.success(
                "게시글 목록 조회 성공",
                postService.getPosts(
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
            @Valid @RequestBody PostUpdateRequestDto request,
            HttpServletRequest httpRequest
    ) {

        HttpSession session = httpRequest.getSession(false);

        int userId = (int) session.getAttribute("user_id");

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
            HttpServletRequest httpRequest
    ) {

        HttpSession session = httpRequest.getSession(false);

        int userId = (int) session.getAttribute("user_id");

        postService.deletePost(
                userId,
                postId
        );

        return ApiResponse.success(
                "게시글 삭제 성공"
        );
    }
}