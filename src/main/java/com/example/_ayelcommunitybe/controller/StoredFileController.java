package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.storedfile.StoredFileUploadResponseDto;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.service.StoredFileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoredFileController {

    private final StoredFileService storedFileService;

    // 프로필 파일 업로드
    @PostMapping("/users/{userId}/files")
    public ApiResponse<StoredFileUploadResponseDto> uploadProfileFile(
            @PathVariable int userId,
            @RequestParam String fileUrl,
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        int sessionUserId =
                (int) session.getAttribute("user_id");

        if (sessionUserId != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }

        StoredFileUploadResponseDto response =
                storedFileService.uploadProfileFile(
                        userId,
                        fileUrl
                );

        return ApiResponse.success(
                "프로필 파일 업로드 성공",
                response
        );
    }

    // 게시글 파일 업로드
    @PostMapping("/posts/{postId}/files")
    public ApiResponse<StoredFileUploadResponseDto> uploadPostFile(
            @PathVariable int postId,
            @RequestParam String fileUrl,
            HttpServletRequest request
    ) {

        HttpSession session =
                request.getSession(false);

        int userId =
                (int) session.getAttribute("user_id");

        StoredFileUploadResponseDto response =
                storedFileService.uploadPostFile(
                        userId,
                        postId,
                        fileUrl
                );

        return ApiResponse.success(
                "게시글 파일 업로드 성공",
                response
        );
    }
}