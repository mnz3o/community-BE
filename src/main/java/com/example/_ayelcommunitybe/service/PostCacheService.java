package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.config.RedisConfig;
import com.example._ayelcommunitybe.dto.post.PostResponseDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCacheService {

    private final PostFinder postFinder;
    private final StoredFileRepository storedFileRepository;
    private final MessageSource messageSource;

    @Cacheable(
            cacheNames = RedisConfig.POST_DETAIL_CACHE,
            key = "#postId",
            sync = true
    )
    public PostResponseDto getPostDetail(int postId) {

        Post post =
                postFinder.findDetailById(postId);

        User user =
                post.getUser();

        boolean isDeleted =
                user.getDeletedAt() != null;

        String nickname = isDeleted
                ? messageSource.getMessage(
                "user.deleted.nickname",
                null,
                LocaleContextHolder.getLocale()
        )
                : user.getNickname();

        String profileFileUrl = null;

        if (!isDeleted) {
            profileFileUrl =
                    storedFileRepository
                            .findByUserAndIsActiveTrue(user)
                            .map(StoredFile::getFileUrl)
                            .orElse(null);
        }

        return new PostResponseDto(
                post.getPostId(),
                user.getUserId(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                getFileUrls(post),
                profileFileUrl,
                post.getCreatedAt(),
                false
        );
    }

    // 활성화된 게시글 파일 URL만 조회
    private List<String> getFileUrls(
            Post post
    ) {

        return post.getFiles()
                .stream()
                .filter(StoredFile::isActive)
                .map(StoredFile::getFileUrl)
                .toList();
    }
}