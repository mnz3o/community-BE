package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostViewCountService {

    private final PostRepository postRepository;

    @Transactional
    public void increase(int postId) {
        postRepository.increaseViewCount(postId);
    }
}