package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.post.PostCreateRequestDto;
import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import com.example._ayelcommunitybe.dto.post.PostPageResponseDto;
import com.example._ayelcommunitybe.dto.post.PostResponseDto;
import com.example._ayelcommunitybe.dto.post.PostUpdateRequestDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.event.PostViewedEvent;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final StoredFileRepository storedFileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messageSource;
    private final UserFinder userFinder;
    private final PostFinder postFinder;
    private final StoredFileService storedFileService;

    // 게시글 작성
    @Transactional(
            rollbackFor = Exception.class
    )
    public int createPost(
            int userId,
            PostCreateRequestDto request,
            MultipartFile file
    ) throws IOException {

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

        if (file != null
                && !file.isEmpty()) {

            storedFileService.savePostFile(
                    savedPost,
                    file
            );
        }

        return savedPost.getPostId();
    }

    // 게시글 검색
    public PostPageResponseDto searchPosts(
            String keyword,
            Integer cursor,
            int limit) {

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        List<PostListResponseDto> posts =
                postRepository.searchPosts(
                        keyword,
                        cursor,
                        PageRequest.of(0, limit + 1)
                );

        return createPageResponse(
                posts,
                limit
        );
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(int postId) {

        Post post = postFinder.findDetailById(postId);

        // 게시글 조회 이벤트 발행
        eventPublisher.publishEvent(
                new PostViewedEvent(postId)
        );

        User user = post.getUser();

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

        Post post =
                postFinder.findDetailById(postId);

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
        List<PostListResponseDto> posts =
                postRepository.findPosts(
                        cursor,
                        PageRequest.of(0, limit + 1)
                );

        return createPageResponse(
                posts,
                limit
        );
    }

    // 게시글 삭제
    @Transactional
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

    // 활성화된 파일 URL만 조회
    private List<String> getFileUrls(
            Post post) {

        return post.getFiles()
                .stream()
                .filter(StoredFile::isActive)
                .map(StoredFile::getFileUrl)
                .toList();
    }

    // 게시글 목록 응답 생성
    private PostPageResponseDto createPageResponse(
            List<PostListResponseDto> posts,
            int limit) {

        boolean hasNext =
                posts.size() > limit;

        // 다음 페이지가 존재하면 마지막 데이터 제거
        if (hasNext) {
            posts.remove(limit);
        }

        Integer nextCursor = null;

        // 다음 요청에 사용할 커서
        if (hasNext) {
            nextCursor = posts.get(
                    posts.size() - 1
            ).postId();
        }

        return new PostPageResponseDto(
                posts,
                nextCursor,
                hasNext
        );
    }
}