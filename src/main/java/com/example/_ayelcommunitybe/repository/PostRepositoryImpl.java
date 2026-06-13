package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.entity.Post;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example._ayelcommunitybe.entity.QPost.post;

@RequiredArgsConstructor
public class PostRepositoryImpl
        implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 게시글 검색
    @Override
    public List<Post> searchPosts(
            String keyword,
            Integer cursor,
            Pageable pageable
    ) {

        return queryFactory
                .selectFrom(post)
                .join(post.user).fetchJoin()
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
}