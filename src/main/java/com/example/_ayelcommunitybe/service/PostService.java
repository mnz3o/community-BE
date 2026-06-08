package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.post.PostCreateRequestDto;
import com.example._ayelcommunitybe.dto.post.PostPageResponseDto;
import com.example._ayelcommunitybe.dto.post.PostResponseDto;
import com.example._ayelcommunitybe.dto.post.PostUpdateRequestDto;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    // 게시글 작성
    @Transactional
    public int createPost(
            int userId,
            PostCreateRequestDto request
    ) {

        User user = findUser(userId);

        Post post = new Post(
                user,
                request.title(),
                request.content()
        );

        Post savedPost = postRepository.save(post);

        return savedPost.getPostId();
    }

    // 게시글 상세 조회
    @Transactional
    public PostResponseDto getPost(
            int postId
    ) {

        Post post = findPost(postId);

        // 조회수 증가
        post.increaseViewCount();

        return new PostResponseDto(
                post.getPostId(),
                post.getTitle(),
                post.getContent(),
                post.getUser().getNickname(),
                post.getViewCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getCreatedAt()
        );
    }

    // 게시글 수정
    @Transactional
    public void updatePost(
            int userId,
            int postId,
            PostUpdateRequestDto request
    ) {

        Post post = findPost(postId);

        // 작성자만 수정 가능
        validatePostOwner(
                post,
                userId
        );

        post.update(
                request.title(),
                request.content()
        );
    }

    // 게시글 목록 조회
    public PostPageResponseDto getPosts(
            Integer cursor,
            int limit
    ) {

        // limit + 1개 조회해서 다음 페이지 존재 여부 확인
        List<Post> posts = postRepository.findPosts(
                cursor,
                PageRequest.of(0, limit + 1)
        );

        boolean hasNext = posts.size() > limit;

        // 다음 페이지가 존재하면 마지막 1개 제거
        if (hasNext) {
            posts.remove(limit);
        }

        List<PostResponseDto> postResponses = posts.stream()
                .map(post -> new PostResponseDto(
                        post.getPostId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getUser().getNickname(),
                        post.getViewCount(),
                        post.getLikeCount(),
                        post.getCommentCount(),
                        post.getCreatedAt()
                ))
                .toList();

        // 다음 요청에 사용할 cursor
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
            int postId
    ) {

        Post post = findPost(postId);

        // 작성자만 삭제 가능
        validatePostOwner(
                post,
                userId
        );

        post.delete();
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
        return postRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.POST_NOT_FOUND
                        )
                );
    }

    private void validatePostOwner(
            Post post,
            int userId
    ) {
        if (post.getUser().getUserId() != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }
}