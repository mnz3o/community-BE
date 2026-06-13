package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.storedfile.StoredFileUploadResponseDto;
import com.example._ayelcommunitybe.service.StoredFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class StoredFileController {

    private final StoredFileService storedFileService;

    // 프로필 파일 업로드
    @PostMapping("/users/{userId}/files")
    public ApiResponse<StoredFileUploadResponseDto> uploadProfileFile(
            @PathVariable int userId,
            @RequestAttribute(SessionConst.USER_ID) int sessionUserId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        StoredFileUploadResponseDto response =
                storedFileService.uploadProfileFile(
                        sessionUserId,
                        userId,
                        file
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
            @RequestAttribute(SessionConst.USER_ID) int userId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        StoredFileUploadResponseDto response =
                storedFileService.uploadPostFile(
                        userId,
                        postId,
                        file
                );

        return ApiResponse.success(
                "게시글 파일 업로드 성공",
                response
        );
    }

    // 프로필 파일 삭제
    @DeleteMapping("/users/{userId}/files")
    public ApiResponse<Void> deleteProfileFile(
            @PathVariable int userId,
            @RequestAttribute(SessionConst.USER_ID) int sessionUserId
    ) {

        storedFileService.deleteProfileFile(
                sessionUserId,
                userId
        );

        return ApiResponse.success(
                "프로필 파일 삭제 성공"
        );
    }

    // 게시글 파일 삭제
    @DeleteMapping("/posts/{postId}/files/{fileId}")
    public ApiResponse<Void> deletePostFile(
            @PathVariable int postId,
            @PathVariable int fileId,
            @RequestAttribute(SessionConst.USER_ID) int userId
    ) {

        storedFileService.deletePostFile(
                userId,
                postId,
                fileId
        );

        return ApiResponse.success(
                "게시글 파일 삭제 성공"
        );
    }
}