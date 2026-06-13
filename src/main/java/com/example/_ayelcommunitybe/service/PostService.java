package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.post.PostCreateRequestDto;
import com.example._ayelcommunitybe.dto.post.PostPageResponseDto;
import com.example._ayelcommunitybe.dto.post.PostResponseDto;
import com.example._ayelcommunitybe.dto.post.PostUpdateRequestDto;
import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.event.PostViewedEvent;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StoredFileRepository storedFileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messageSource;

    // 게시글 작성
    @Transactional
    public int createPost(int userId, PostCreateRequestDto request) {
        User user = findUser(userId);

        Post post = new Post(
                user,
                request.title(),
                request.content()
        );

        Post savedPost = postRepository.save(post);
        return savedPost.getPostId();
    }

    // 게시글 검색
    public PostPageResponseDto searchPosts(
            String keyword,
            Integer cursor,
            int limit) {

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        List<Post> posts = postRepository.searchPosts(
                keyword,
                cursor,
                PageRequest.of(0, limit + 1)
        );

        boolean hasNext = posts.size() > limit;

        if (hasNext) {
            posts.remove(limit);
        }

        List<PostListResponseDto> postResponses = posts.stream()
                .map(post -> {
                    User user = post.getUser();
                    boolean isDeleted = user.getDeletedAt() != null;
                    String nickname = isDeleted ? messageSource.getMessage("user.deleted.nickname", null, LocaleContextHolder.getLocale()) : user.getNickname();
                    String profileFileUrl = null;
                    if (!isDeleted) {
                        profileFileUrl = storedFileRepository.findByUserAndIsActiveTrue(user)
                                .map(StoredFile::getFileUrl)
                                .orElse(null);
                    }
                    return new PostListResponseDto(
                            post.getPostId(),
                            post.getTitle(),
                            nickname,
                            post.getViewCount(),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            profileFileUrl,
                            post.getCreatedAt()
                    );
                })
                .toList();

        // 다음 요청에 사용할 커서
        Integer nextCursor = null;

        if (hasNext) {
            nextCursor = postResponses.get(
                    postResponses.size() - 1
            ).postId();
        }

        return new PostPageResponseDto(
                postResponses,
                nextCursor,
                hasNext
        );
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(int postId) {

        Post post = findPost(postId);

        // 게시글 조회 이벤트 발행
        eventPublisher.publishEvent(new PostViewedEvent(postId));

        User user = post.getUser();
        boolean isDeleted = user.getDeletedAt() != null;
        String nickname = isDeleted ? messageSource.getMessage("user.deleted.nickname", null, LocaleContextHolder.getLocale()) : user.getNickname();
        String profileFileUrl = null;
        if (!isDeleted) {
            profileFileUrl = storedFileRepository.findByUserAndIsActiveTrue(user)
                    .map(StoredFile::getFileUrl)
                    .orElse(null);
        }

        return new PostResponseDto(
                post.getPostId(),
                post.getUser().getUserId(),
                post.getTitle(),
                post.getContent(),
                nickname,
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                getFileUrls(post),
                profileFileUrl,
                post.getCreatedAt()
        );
    }

    // 게시글 수정
    @Transactional
    public void updatePost(
            int userId,
            int postId,
            PostUpdateRequestDto request) {

        Post post = findPost(postId);

        validatePostOwner(post, userId);

        post.update(
                request.title(),
                request.content()
        );
    }

    // 게시글 목록 조회
    public PostPageResponseDto getPosts(
            Integer cursor,
            int limit) {

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        List<Post> posts = postRepository.findPosts(
                cursor,
                PageRequest.of(0, limit + 1)
        );

        boolean hasNext = posts.size() > limit;

        // 다음 페이지가 존재하면 마지막 데이터 제거
        if (hasNext) {
            posts.remove(limit);
        }

        List<PostListResponseDto> postResponses = posts.stream()
                .map(post -> {
                    User user = post.getUser();
                    boolean isDeleted = user.getDeletedAt() != null;
                    String nickname = isDeleted ? messageSource.getMessage("user.deleted.nickname", null, LocaleContextHolder.getLocale()) : user.getNickname();
                    String profileFileUrl = null;
                    if (!isDeleted) {
                        profileFileUrl = storedFileRepository.findByUserAndIsActiveTrue(user)
                                .map(StoredFile::getFileUrl)
                                .orElse(null);
                    }
                    return new PostListResponseDto(
                            post.getPostId(),
                            post.getTitle(),
                            nickname,
                            post.getViewCount(),
                            post.getLikeCount(),
                            post.getCommentCount(),
                            profileFileUrl,
                            post.getCreatedAt()
                    );
                })
                .toList();

        // 다음 요청에 사용할 커서
        Integer nextCursor = null;

        if (hasNext) {
            nextCursor = postResponses.get(
                    postResponses.size() - 1
            ).postId();
        }

        return new PostPageResponseDto(
                postResponses,
                nextCursor,
                hasNext
        );
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(
            int userId,
            int postId) {

        Post post = findPost(postId);

        validatePostOwner(post, userId);

        post.delete();
    }

    private User findUser(int userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPost(int postId) {
        return postRepository.findPostDetail(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private void validatePostOwner(
            Post post,
            int userId) {

        // 작성자만 게시글 수정 및 삭제 가능
        if (post.getUser().getUserId() != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    // 활성화된 파일 URL만 조회
    private List<String> getFileUrls(Post post) {
        return post.getFiles()
                .stream()
                .filter(StoredFile::isActive)
                .map(StoredFile::getFileUrl)
                .toList();
    }
}