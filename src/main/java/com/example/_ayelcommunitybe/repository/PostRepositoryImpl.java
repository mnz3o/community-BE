package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example._ayelcommunitybe.entity.QPost.post;
import static com.example._ayelcommunitybe.entity.QStoredFile.storedFile;

@RequiredArgsConstructor
public class PostRepositoryImpl
        implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 게시글 검색
    @Override
    public List<PostListResponseDto> searchPosts(
            String keyword,
            Integer cursor,
            Pageable pageable
    ) {

        return postListQuery()
                .where(
                        post.deletedAt.isNull(),
                        post.title.contains(keyword),

                        // 커서 기반 페이징
                        cursor == null
                                ? null
                                : post.postId.lt(cursor)
                )
                .orderBy(post.postId.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // 게시글 목록 조회
    @Override
    public List<PostListResponseDto> findPosts(
            Integer cursor,
            Pageable pageable
    ) {

        return postListQuery()
                .where(
                        post.deletedAt.isNull(),

                        // 커서 기반 페이징
                        cursor == null
                                ? null
                                : post.postId.lt(cursor)
                )
                .orderBy(post.postId.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    // 게시글 목록 조회 공통 쿼리
    private JPAQuery<PostListResponseDto> postListQuery() {

        return queryFactory
                .select(
                        Projections.constructor(
                                PostListResponseDto.class,
                                post.postId,
                                post.title,
                                post.user.nickname,
                                post.viewCount,
                                post.likeCount,
                                post.commentCount,
                                storedFile.fileUrl,
                                post.createdAt
                        )
                )
                .from(post)
                .join(post.user)
                .leftJoin(storedFile)
                .on(
                        storedFile.user.eq(post.user),
                        storedFile.isActive.isTrue()
                );
    }
}