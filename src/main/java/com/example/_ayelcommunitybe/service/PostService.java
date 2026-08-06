package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.config.RedisConfig;
import com.example._ayelcommunitybe.constant.PostSearchType;
import com.example._ayelcommunitybe.constant.PostSortType;
import com.example._ayelcommunitybe.dto.post.*;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.event.PostViewedEvent;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.PostLikeRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final StoredFileRepository storedFileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final UserFinder userFinder;
    private final PostFinder postFinder;
    private final PostCacheService postCacheService;
    private final PostLikeRepository postLikeRepository;

    // 게시글 작성
    @Transactional(
            rollbackFor = Exception.class
    )
    public int createPost(
            int userId,
            PostCreateRequestDto request
    ) {

        User user =
                userFinder.findById(userId);

        Post post =
                new Post(
                        user,
                        request.title(),
                        request.content()
                );

        Post savedPost =
                postRepository.save(post);

        if (request.fileUrls() != null) {
            for (String fileUrl : request.fileUrls()) {
                storedFileRepository.save(
                        new StoredFile(
                                null,
                                savedPost,
                                fileUrl
                        )
                );
            }
        }

        return savedPost.getPostId();
    }

    // 게시글 검색
    public PostPageResponseDto searchPosts(
            String keyword,
            PostSearchType searchType,
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            int limit
    ) {

        validateSearchKeyword(keyword);

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        List<PostListResponseDto> posts =
                postRepository.searchPosts(
                        keyword.trim(),
                        searchType,
                        sort,
                        cursorSortValue,
                        cursorPostId,
                        PageRequest.of(0, limit + 1)
                );

        return createPageResponse(
                posts,
                limit,
                sort
        );
    }

    // 게시글 상세 조회
    public PostResponseDto getPost(
            Integer userId,
            int postId
    ) {

        PostResponseDto cachedPost =
                postCacheService.getPostDetail(postId);

        boolean isLiked =
                userId != null &&
                        postLikeRepository
                                .existsByUser_UserIdAndPost_PostId(
                                        userId,
                                        postId
                                );

        // 캐시 HIT 여부와 관계없이 조회 이벤트 발행
        eventPublisher.publishEvent(
                new PostViewedEvent(postId)
        );

        return new PostResponseDto(
                cachedPost.postId(),
                cachedPost.userId(),
                cachedPost.title(),
                cachedPost.content(),
                cachedPost.nickname(),
                cachedPost.viewCount(),
                cachedPost.likeCount(),
                cachedPost.commentCount(),
                cachedPost.fileUrls(),
                cachedPost.profileFileUrl(),
                cachedPost.createdAt(),
                isLiked
        );
    }

    // 게시글 수정
    @Transactional(
            rollbackFor = Exception.class
    )
    @CacheEvict(
            cacheNames = RedisConfig.POST_DETAIL_CACHE,
            key = "#postId"
    )
    public void updatePost(
            int userId,
            int postId,
            PostUpdateRequestDto request
    ) {

        Post post =
                postFinder.findDetailById(postId);

        validatePostOwner(post, userId);

        post.update(
                request.title(),
                request.content()
        );

        // 기존 파일 중 선택 해제된 것만 비활성화
        post.getFiles().forEach(file -> {
            if (request.fileUrls() == null ||
                    !request.fileUrls().contains(file.getFileUrl())) {

                file.deactivate();
            }
        });

        // 새 파일 추가
        if (request.fileUrls() != null) {

            List<String> existingUrls =
                    post.getFiles()
                            .stream()
                            .filter(StoredFile::isActive)
                            .map(StoredFile::getFileUrl)
                            .toList();

            request.fileUrls().stream()
                    .filter(fileUrl -> !existingUrls.contains(fileUrl))
                    .forEach(fileUrl ->
                            storedFileRepository.save(
                                    new StoredFile(
                                            null,
                                            post,
                                            fileUrl
                                    )
                            )
                    );
        }
    }

    // 게시글 목록 조회
    public PostPageResponseDto getPosts(
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            int limit
    ) {

        List<PostListResponseDto> posts =
                postRepository.findPosts(
                        sort,
                        cursorSortValue,
                        cursorPostId,
                        PageRequest.of(0, limit + 1)
                );

        return createPageResponse(
                posts,
                limit,
                sort
        );
    }

    // 주간 인기글 조회
    public List<PostListResponseDto> getWeeklyPopularPosts() {

        LocalDateTime startDate =
                LocalDateTime.now().minusDays(7);

        return postRepository.findWeeklyPopularPosts(
                startDate
        );
    }

    // 게시글 삭제
    @Transactional
    @CacheEvict(
            cacheNames = RedisConfig.POST_DETAIL_CACHE,
            key = "#postId"
    )
    public void deletePost(
            int userId,
            int postId) {

        Post post =
                postFinder.findDetailById(postId);

        validatePostOwner(post, userId);

        post.delete();
    }

    private void validatePostOwner(
            Post post,
            int userId) {

        // 작성자만 게시글 수정 및 삭제 가능
        if (post.getUser().getUserId() != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }

    private void validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_SEARCH_KEYWORD
            );
        }
    }

    // 게시글 목록 응답 생성
    private PostPageResponseDto createPageResponse(
            List<PostListResponseDto> posts,
            int limit,
            PostSortType sort
    ) {

        boolean hasNext = posts.size() > limit;

        if (hasNext) {
            posts = List.copyOf(
                    posts.subList(0, limit)
            );
        } else {
            posts = List.copyOf(posts);
        }

        PostCursorDto nextCursor = null;

        if (hasNext && !posts.isEmpty()) {

            PostListResponseDto lastPost =
                    posts.get(posts.size() - 1);

            int sortValue =
                    switch (sort) {
                        case LATEST -> lastPost.postId();
                        case VIEW -> lastPost.viewCount();
                        case LIKE -> lastPost.likeCount();
                    };

            nextCursor = new PostCursorDto(
                    sortValue,
                    lastPost.postId()
            );
        }

        return new PostPageResponseDto(
                posts,
                nextCursor,
                hasNext
        );
    }
}