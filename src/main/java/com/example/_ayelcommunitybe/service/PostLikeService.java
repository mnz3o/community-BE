package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.PostLike;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostLikeRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 좋아요 추가
    @Transactional
    public void createLike(
            int userId,
            int postId
    ) {

        User user = findUser(userId);
        Post post = findPost(postId);

        // 중복 좋아요 방지
        if (postLikeRepository.findByUserAndPost(user, post).isPresent()) {
            throw new CustomException(
                    ErrorCode.ALREADY_LIKED
            );
        }

        PostLike postLike = new PostLike(
                user,
                post
        );

        postLikeRepository.save(postLike);

        // 게시글 좋아요 수 증가
        post.increaseLikeCount();

        postRepository.save(post);
    }

    // 좋아요 취소
    @Transactional
    public void deleteLike(
            int userId,
            int postId
    ) {

        User user = findUser(userId);
        Post post = findPost(postId);

        PostLike postLike = findPostLike(
                user,
                post
        );

        postLikeRepository.delete(postLike);

        // 게시글 좋아요 수 감소
        post.decreaseLikeCount();

        postRepository.save(post);
    }

    private User findUser(
            int userId
    ) {
        return userRepository.findById(userId)
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

    private PostLike findPostLike(
            User user,
            Post post
    ) {
        return postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.LIKE_NOT_FOUND
                        )
                );
    }
}