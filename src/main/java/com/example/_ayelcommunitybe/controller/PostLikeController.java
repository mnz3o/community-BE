package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 좋아요 추가
    @PostMapping
    public ApiResponse<Void> createLike(
            @PathVariable int postId,
            @RequestAttribute(SessionConst.USER_ID) int userId
    ) {

        postLikeService.createLike(
                userId,
                postId
        );

        return ApiResponse.success(
                "좋아요 성공"
        );
    }

    // 좋아요 취소
    @DeleteMapping
    public ApiResponse<Void> deleteLike(
            @PathVariable int postId,
            @RequestAttribute(SessionConst.USER_ID) int userId
    ) {

        postLikeService.deleteLike(userId, postId);

        return ApiResponse.success(
                "좋아요 취소 성공"
        );
    }
}