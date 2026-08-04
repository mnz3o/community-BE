package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.constant.PostSearchType;
import com.example._ayelcommunitybe.constant.PostSortType;
import com.example._ayelcommunitybe.dto.post.PostListResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.example._ayelcommunitybe.entity.QPost.post;
import static com.example._ayelcommunitybe.entity.QStoredFile.storedFile;

@RequiredArgsConstructor
public class PostRepositoryImpl
        implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final int CONTENT_PREVIEW_LENGTH = 100;

    // 게시글 검색
    @Override
    public List<PostListResponseDto> searchPosts(
            String keyword,
            PostSearchType searchType,
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId,
            Pageable pageable
    ) {

        return postListQuery()
                .where(
                        // 삭제된 게시글 제외
                        post.deletedAt.isNull(),

                        // 제목, 제목+내용, 작성자 검색 조건
                        getSearchCondition(
                                keyword,
                                searchType
                        ),

                        // 정렬 방식에 따른 커서 조건
                        getCursorCondition(
                                sort,
                                cursorSortValue,
                                cursorPostId
                        )
                )
                // 최신순, 조회수순, 좋아요순 정렬
                .orderBy(
                        getOrderSpecifiers(sort)
                )
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
                        // 삭제된 게시글 제외
                        post.deletedAt.isNull(),

                        // 인기글 조회 시 최근 7일 게시글만 포함
                        getPopularPeriodCondition(sort),

                        // 정렬 방식에 따른 커서 조건
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

    // 정렬 방식에 따른 정렬 기준 생성
    private OrderSpecifier<?>[] getOrderSpecifiers(
            PostSortType sort
    ) {

        return switch (sort) {

            // 게시글 ID가 클수록 최신 게시글
            case LATEST ->
                    new OrderSpecifier[]{
                            post.postId.desc()
                    };

            // 조회수가 같으면 최신 게시글 우선
            case VIEW ->
                    new OrderSpecifier[]{
                            post.viewCount.desc(),
                            post.postId.desc()
                    };

            // 좋아요 수가 같으면 최신 게시글 우선
            case LIKE ->
                    new OrderSpecifier[]{
                            post.likeCount.desc(),
                            post.postId.desc()
                    };

            // 인기 점수가 같으면 최신 게시글 우선
            case POPULAR ->
                    new OrderSpecifier[]{
                            getPopularScore().desc(),
                            post.postId.desc()
                    };
        };
    }

    // 정렬 방식에 따른 복합 커서 조건 생성
    private BooleanExpression getCursorCondition(
            PostSortType sort,
            Integer cursorSortValue,
            Integer cursorPostId
    ) {

        // 첫 페이지 요청
        if (cursorSortValue == null && cursorPostId == null) {
            return null;
        }

        // 복합 커서 값은 반드시 함께 전달
        if (cursorSortValue == null || cursorPostId == null) {
            throw new IllegalArgumentException(
                    "cursorSortValue와 cursorPostId는 함께 전달해야 합니다."
            );
        }

        return switch (sort) {

            // 최신순은 게시글 ID만 비교
            case LATEST ->
                    post.postId.lt(cursorPostId);

            // 조회수가 작거나, 조회수가 같으면서 게시글 ID가 작은 게시글 조회
            case VIEW ->
                    post.viewCount.lt(cursorSortValue)
                            .or(
                                    post.viewCount.eq(cursorSortValue)
                                            .and(
                                                    post.postId.lt(cursorPostId)
                                            )
                            );

            // 좋아요 수가 작거나, 좋아요 수가 같으면서 게시글 ID가 작은 게시글 조회
            case LIKE ->
                    post.likeCount.lt(cursorSortValue)
                            .or(
                                    post.likeCount.eq(cursorSortValue)
                                            .and(
                                                    post.postId.lt(cursorPostId)
                                            )
                            );

            // 인기 점수가 작거나, 인기 점수가 같으면서 게시글 ID가 작은 게시글 조회
            case POPULAR -> {
                NumberExpression<Integer> popularScore =
                        getPopularScore();

                yield popularScore.lt(cursorSortValue)
                        .or(
                                popularScore.eq(cursorSortValue)
                                        .and(
                                                post.postId.lt(cursorPostId)
                                        )
                        );
            }
        };
    }

    // 게시글 목록과 검색에서 사용하는 공통 조회 쿼리
    private JPAQuery<PostListResponseDto> postListQuery() {

        return queryFactory
                .select(
                        Projections.constructor(
                                PostListResponseDto.class,
                                post.postId,
                                post.title,
                                getContentPreview(),
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

                // 작성자의 활성화된 프로필 이미지 조회
                .leftJoin(storedFile)
                .on(
                        storedFile.user.eq(post.user),
                        storedFile.isActive.isTrue()
                );
    }

    // 인기글 점수 계산 {5 * (조회수 0.2 * 좋아요수 5 * 댓글수 * 3)}
    private NumberExpression<Integer> getPopularScore() {

        return post.viewCount
                .add(post.likeCount.multiply(25))
                .add(post.commentCount.multiply(15));
    }

    // 인기글 조회 시 최근 7일 조건 적용
    private BooleanExpression getPopularPeriodCondition(
            PostSortType sort
    ) {

        if (sort != PostSortType.POPULAR) {
            return null;
        }

        return post.createdAt.goe(
                LocalDateTime.now().minusDays(7)
        );
    }

    // 검색 범위에 따른 검색 조건 생성
    private BooleanExpression getSearchCondition(
            String keyword,
            PostSearchType searchType
    ) {

        return switch (searchType) {

            // 제목에서 검색
            case TITLE ->
                    post.title.contains(keyword);

            // 제목 또는 내용에서 검색
            case TITLE_CONTENT ->
                    post.title.contains(keyword)
                            .or(
                                    post.content.contains(keyword)
                            );

            // 작성자 닉네임에서 검색
            case AUTHOR ->
                    post.user.nickname.contains(keyword);
        };
    }

    // 게시글 목록에 표시할 본문 앞 100자 조회
    private StringExpression getContentPreview() {
        return Expressions.stringTemplate(
                "substring({0}, 1, {1})",
                post.content,
                CONTENT_PREVIEW_LENGTH
        );
    }
}