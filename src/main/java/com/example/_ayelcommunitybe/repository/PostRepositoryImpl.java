package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.constant.PostSortType;
import com.querydsl.core.types.OrderSpecifier;
import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import com.querydsl.core.types.dsl.BooleanExpression;
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
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            Pageable pageable
    ) {

        return postListQuery()
                .where(
                        post.deletedAt.isNull(),
                        getCursorCondition(
                                sort,
                                cursorSortValue,
                                cursorPostId
                        )
                )
                .orderBy(
                        getOrderSpecifiers(sort)
                )
                .limit(pageable.getPageSize())
                .fetch();
    }

    // 정렬 기준 생성
    private OrderSpecifier<?>[] getOrderSpecifiers(
            PostSortType sort
    ) {

        return switch (sort) {

            case LATEST ->
                    new OrderSpecifier[]{
                            post.postId.desc()
                    };

            case VIEW ->
                    new OrderSpecifier[]{
                            post.viewCount.desc(),
                            post.postId.desc()
                    };

            case LIKE ->
                    new OrderSpecifier[]{
                            post.likeCount.desc(),
                            post.postId.desc()
                    };
        };
    }

    // 복합 커서 조건 생성
    private BooleanExpression getCursorCondition(
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId
    ) {

        if (cursorSortValue == null && cursorPostId == null) {
            return null;
        }

        if (cursorSortValue == null || cursorPostId == null) {
            throw new IllegalArgumentException(
                    "cursorSortValue와 cursorPostId는 함께 전달해야 합니다."
            );
        }

        return switch (sort) {

            case LATEST ->
                    post.postId.lt(cursorPostId);

            case VIEW ->
                    post.viewCount.lt(cursorSortValue)
                            .or(
                                    post.viewCount.eq(cursorSortValue)
                                            .and(post.postId.lt(cursorPostId))
                            );

            case LIKE ->
                    post.likeCount.lt(cursorSortValue)
                            .or(
                                    post.likeCount.eq(cursorSortValue)
                                            .and(post.postId.lt(cursorPostId))
                            );
        };
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