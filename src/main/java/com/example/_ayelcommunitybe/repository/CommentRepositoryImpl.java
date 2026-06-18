package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.example._ayelcommunitybe.entity.QComment.comment;
import static com.example._ayelcommunitybe.entity.QStoredFile.storedFile;

@RequiredArgsConstructor
public class CommentRepositoryImpl
        implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 댓글 목록 조회
    @Override
    public List<CommentResponseDto> findComments(
            int postId,
            Integer cursor,
            Pageable pageable
    ) {

        return queryFactory
                .select(
                        Projections.constructor(
                                CommentResponseDto.class,
                                comment.commentId,
                                comment.user.userId,
                                comment.content,
                                comment.user.nickname,
                                storedFile.fileUrl,
                                comment.createdAt
                        )
                )
                .from(comment)
                .join(comment.user)
                .leftJoin(storedFile)
                .on(
                        storedFile.user.eq(comment.user),
                        storedFile.isActive.isTrue()
                )
                .where(
                        comment.deletedAt.isNull(),
                        comment.post.postId.eq(postId),

                        // 커서 기반 페이징
                        cursor == null
                                ? null
                                : comment.commentId.lt(cursor)
                )
                .orderBy(comment.commentId.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
