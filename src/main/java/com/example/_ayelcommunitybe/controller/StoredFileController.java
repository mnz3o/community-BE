package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.storedfile.StoredFileUploadResponseDto;
import com.example._ayelcommunitybe.service.StoredFileService;
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

            @RequestAttribute("user_id")
            int sessionUserId,

            @RequestParam String fileUrl
    ) {
        StoredFileUploadResponseDto response =
                storedFileService.uploadProfileFile(
                        sessionUserId,
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

            @RequestAttribute("user_id")
            int userId,

            @RequestParam String fileUrl
    ) {

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