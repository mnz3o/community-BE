package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.config.FileProperties;
import com.example._ayelcommunitybe.dto.storedfile.StoredFileUploadResponseDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoredFileService {

    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FileProperties fileProperties;

    // 게시글 파일 업로드
    @Transactional
    public StoredFileUploadResponseDto uploadPostFile(
            int userId,
            int postId,
            MultipartFile file) throws IOException {

        Post post = findPost(postId);

        // 작성자만 게시글 파일 관리 가능
        validatePostOwner(post, userId);

        String fileUrl = saveFile(file);

        StoredFile storedFile = new StoredFile(
                null,
                post,
                fileUrl
        );

        StoredFile savedFile =
                storedFileRepository.save(storedFile);

        return new StoredFileUploadResponseDto(
                savedFile.getFileId(),
                savedFile.getFileUrl()
        );
    }

    // 프로필 파일 업로드
    @Transactional
    public StoredFileUploadResponseDto uploadProfileFile(
            int sessionUserId,
            int userId,
            MultipartFile file) throws IOException {

        // 본인만 프로필 수정 가능
        validateUserOwner(sessionUserId, userId);

        User user = findUser(userId);

        // 기존 프로필 파일은 비활성화
        storedFileRepository.findByUserAndIsActiveTrue(user)
                .ifPresent(existing -> {
                    storedFileRepository.delete(existing);
                    storedFileRepository.flush();
                });

        String fileUrl = saveFile(file);

        StoredFile storedFile = new StoredFile(
                user,
                null,
                fileUrl
        );

        StoredFile savedFile =
                storedFileRepository.save(storedFile);

        return new StoredFileUploadResponseDto(
                savedFile.getFileId(),
                savedFile.getFileUrl()
        );
    }

    // 게시글 파일 삭제
    @Transactional
    public void deletePostFile(
            int userId,
            int postId,
            int fileId) {

        StoredFile file = storedFileRepository
                .findByFileIdAndIsActiveTrue(fileId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.FILE_NOT_FOUND));

        Post post = file.getPost();

        if (post == null || post.getPostId() != postId) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        validatePostOwner(post, userId);

        file.deactivate();
    }

    // 프로필 파일 삭제
    @Transactional
    public void deleteProfileFile(
            int sessionUserId,
            int userId) {

        validateUserOwner(sessionUserId, userId);

        User user = findUser(userId);

        StoredFile profileFile =
                storedFileRepository.findByUserAndIsActiveTrue(user)
                        .orElseThrow(() ->
                                new CustomException(ErrorCode.FILE_NOT_FOUND));

        profileFile.deactivate();
    }

    private User findUser(int userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPost(int postId) {
        return postRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(
            Post post,
            int userId) {

        // 작성자만 게시글 파일 관리 가능
        if (post.getUser().getUserId() != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateUserOwner(
            int sessionUserId,
            int userId) {

        // 본인만 프로필 수정 가능
        if (sessionUserId != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private String saveFile(
            MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();

        if (originalName == null) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        String extension = originalName.substring(
                originalName.lastIndexOf(".") + 1
        ).toLowerCase();

        // 허용된 확장자만 업로드 가능
        if (!fileProperties.getAllowedExtensions().contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        // UUID를 사용해 파일명 중복 방지
        String fileName =
                UUID.randomUUID() + "_" + originalName;

        Path uploadPath = Paths.get(fileProperties.getUploadDir())
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);

        System.out.println("저장 경로 = " + filePath);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + fileName;
    }
}