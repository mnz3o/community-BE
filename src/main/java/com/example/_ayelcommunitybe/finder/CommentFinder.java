package com.example._ayelcommunitybe.finder;

import com.example._ayelcommunitybe.entity.Comment;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentFinder {

    private final CommentRepository commentRepository;

    // 댓글 조회
    public Comment findById(int commentId) {
        return commentRepository.findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }
}