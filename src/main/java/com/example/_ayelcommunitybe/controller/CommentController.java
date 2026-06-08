package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.comment.CommentCreateResponseDto;
import com.example._ayelcommunitybe.dto.comment.CommentRequestDto;
import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import com.example._ayelcommunitybe.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<CommentCreateResponseDto> createComment(
            @PathVariable int postId,
            @RequestAttribute("user_id")
            int userId,
            @Valid @RequestBody CommentRequestDto request
    ) {

        int commentId = commentService.createComment(
                userId,
                postId,
                request
        );

        return ApiResponse.success(
                "댓글 작성 성공",
                new CommentCreateResponseDto(commentId)
        );
    }

    // 게시글의 댓글 목록 조회
    @GetMapping
    public ApiResponse<List<CommentResponseDto>> getComments(
            @PathVariable int postId
    ) {

        return ApiResponse.success(
                "댓글 목록 조회 성공",
                commentService.getComments(postId)
        );
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<Void> updateComment(
            @PathVariable int commentId,

            @RequestAttribute("user_id")
            int userId,

            @Valid @RequestBody CommentRequestDto request
    ) {

        commentService.updateComment(
                userId,
                commentId,
                request
        );

        return ApiResponse.success(
                "댓글 수정 성공"
        );
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @PathVariable int commentId,

            @RequestAttribute("user_id")
            int userId
    ) {

        commentService.deleteComment(
                userId,
                commentId
        );

        return ApiResponse.success(
                "댓글 삭제 성공"
        );
    }
}