package com.example._ayelcommunitybe.finder;

import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostFinder {

    private final PostRepository postRepository;

    // 게시글 조회
    public Post findById(int postId) {
        return postRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    // 게시글 상세 조회
    public Post findDetailById(int postId) {
        return postRepository.findPostDetail(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}