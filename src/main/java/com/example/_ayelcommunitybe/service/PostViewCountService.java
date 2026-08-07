package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.config.RedisConfig;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostViewCountService {

    private final PostRepository postRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Caching(evict = {
            @CacheEvict(
                    cacheNames = RedisConfig.POST_DETAIL_CACHE,
                    key = "#postId"
            ),
            @CacheEvict(
                    cacheNames = RedisConfig.POST_LIST_FIRST_PAGE_CACHE,
                    allEntries = true
            ),
            @CacheEvict(
                    cacheNames = RedisConfig.WEEKLY_POPULAR_CACHE,
                    allEntries = true
            )
    })
    public void increase(int postId) {

        int updatedRows = postRepository.increaseViewCount(postId);

        if (updatedRows == 0) {
            throw new CustomException(
                    ErrorCode.POST_NOT_FOUND
            );
        }
    }
}