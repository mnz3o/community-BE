package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoredFileService {

    private final StoredFileRepository storedFileRepository;
    private final UserFinder userFinder;
    private final PostFinder postFinder;

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

        User user = userFinder.findById(userId);

        StoredFile profileFile =
                storedFileRepository.findByUserAndIsActiveTrue(user)
                        .orElseThrow(() ->
                                new CustomException(ErrorCode.FILE_NOT_FOUND));

        profileFile.deactivate();
    }

    private void validatePostOwner(
            Post post,
            int userId) {

        if (post.getUser().getUserId() != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateUserOwner(
            int sessionUserId,
            int userId) {

        if (sessionUserId != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}