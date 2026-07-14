package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.storedfile.StoredFileUploadResponseDto;
import com.example._ayelcommunitybe.service.S3Service;
import com.example._ayelcommunitybe.service.StoredFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@RestController
@RequiredArgsConstructor
public class StoredFileController {

    private final StoredFileService storedFileService;
    private final S3Service s3Service;

    @PostMapping("/files/presigned-url")
    public ApiResponse<StoredFileUploadResponseDto> getPresignedUrl(
            @RequestParam String fileName,
            @RequestParam String contentType
    ) {

        String objectKey = s3Service.createObjectKey(fileName);

        PresignedPutObjectRequest presignedRequest =
                s3Service.createPresignedPutObjectRequest(
                        objectKey,
                        contentType
                );

        StoredFileUploadResponseDto response =
                new StoredFileUploadResponseDto(
                        presignedRequest.url().toString(),
                        s3Service.createFileUrl(objectKey)
                );

        return ApiResponse.success(
                "Presigned URL 발급 성공",
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