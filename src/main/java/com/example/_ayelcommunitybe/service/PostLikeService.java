package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.PostLike;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.PostLikeRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserFinder userFinder;
    private final PostFinder postFinder;

    // 좋아요 추가
    @Transactional
    public void createLike(
            int userId,
            int postId) {

        User user = userFinder.findById(userId);
        Post post = postFinder.findById(postId);

        // 중복 좋아요 방지
        if (postLikeRepository.findByUserAndPost(user, post).isPresent()) {
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        PostLike postLike = new PostLike(user, post);

        postLikeRepository.save(postLike);

        // 좋아요 수 동기화
        postRepository.increaseLikeCount(postId);
    }

    // 좋아요 취소
    @Transactional
    public void deleteLike(
            int userId,
            int postId) {

        User user = userFinder.findById(userId);
        Post post = postFinder.findById(postId);

        PostLike postLike = findPostLike(user, post);

        postLikeRepository.delete(postLike);

        // 좋아요 수 동기화
        postRepository.decreaseLikeCount(postId);
    }

    private PostLike findPostLike(
            User user,
            Post post) {

        return postLikeRepository.findByUserAndPost(user, post)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.LIKE_NOT_FOUND));
    }
}