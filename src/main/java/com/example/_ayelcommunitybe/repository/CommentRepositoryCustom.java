package com.example._ayelcommunitybe.repository;

import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepositoryCustom {

    // 댓글 목록 조회
    List<CommentResponseDto> findComments(
            int postId,
            Integer cursor,
            Pageable pageable
    );
}
