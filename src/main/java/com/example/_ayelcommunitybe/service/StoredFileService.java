package com.example._ayelcommunitybe.service;

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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoredFileService {

    private final StoredFileRepository storedFileRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 게시글 파일 업로드
    @Transactional
    public StoredFileUploadResponseDto uploadPostFile(
            int userId,
            int postId,
            String fileUrl
    ) {

        Post post = findPost(postId);

        // 작성자만 파일 업로드 가능
        if (post.getUser().getUserId() != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }

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
            int userId,
            String fileUrl
    ) {

        User user = findUser(userId);

        // 기존 프로필 파일 비활성화
        storedFileRepository
                .findByUserAndIsActiveTrue(user)
                .ifPresent(StoredFile::deactivate);

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

    private User findUser(
            int userId
    ) {
        return userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private Post findPost(
            int postId
    ) {
        return postRepository
                .findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.POST_NOT_FOUND
                        )
                );
    }
}