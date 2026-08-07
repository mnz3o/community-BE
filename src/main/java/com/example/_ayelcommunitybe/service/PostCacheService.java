package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.config.RedisConfig;
import com.example._ayelcommunitybe.dto.post.PostResponseDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCacheService {

    private final PostFinder postFinder;
    private final StoredFileRepository storedFileRepository;
    private final MessageSource messageSource;
    private final StringRedisTemplate redisTemplate;
    private static final String POST_NOT_FOUND_KEY_PREFIX = "postNotFound:";
    private static final Duration POST_NOT_FOUND_TTL = Duration.ofSeconds(30);

    @Cacheable(
            cacheNames = RedisConfig.POST_DETAIL_CACHE,
            key = "#postId",
            sync = true
    )
    public PostResponseDto getPostDetail(int postId) {

        String notFoundKey = POST_NOT_FOUND_KEY_PREFIX + postId;

        // 존재하지 않는 게시글로 확인된 ID는
        // DB를 다시 조회하지 않고 바로 예외 처리
        if (Boolean.TRUE.equals(
                redisTemplate.hasKey(notFoundKey)
        )) {
            throw new CustomException(
                    ErrorCode.POST_NOT_FOUND
            );
        }

        Post post;

        try {
            post = postFinder.findDetailById(postId);
        } catch (CustomException exception) {

            // 존재하지 않는 게시글 ID는 일정 시간 캐싱하여 DB 조회를 방지
            if (exception.getErrorCode() == ErrorCode.POST_NOT_FOUND) {

                redisTemplate.opsForValue().set(
                        notFoundKey,
                        "1",
                        POST_NOT_FOUND_TTL
                );
            }

            throw exception;
        }

        User user = post.getUser();

        boolean isDeleted = user.getDeletedAt() != null;

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