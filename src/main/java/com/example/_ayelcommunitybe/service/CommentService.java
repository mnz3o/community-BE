package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.comment.CommentPageResponseDto;
import com.example._ayelcommunitybe.dto.comment.CommentRequestDto;
import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import com.example._ayelcommunitybe.entity.Comment;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.finder.CommentFinder;
import com.example._ayelcommunitybe.finder.PostFinder;
import com.example._ayelcommunitybe.finder.UserFinder;
import com.example._ayelcommunitybe.repository.CommentRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserFinder userFinder;
    private final PostFinder postFinder;
    private final CommentFinder commentFinder;

    // 댓글 작성
    @Transactional
    public int createComment(
            int userId,
            int postId,
            CommentRequestDto request) {

        User user = userFinder.findById(userId);
        Post post = postFinder.findById(postId);

        Comment comment =
                new Comment(user, post, request.content());

        Comment savedComment =
                commentRepository.save(comment);

        // 댓글 수 동기화
        postRepository.increaseCommentCount(postId);

        return savedComment.getCommentId();
    }

    // 댓글 목록 조회
    public CommentPageResponseDto getComments(
            int postId,
            Integer cursor,
            int limit) {

        // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
        List<CommentResponseDto> comments =
                commentRepository.findComments(
                        postId,
                        cursor,
                        PageRequest.of(0, limit + 1)
                );

        boolean hasNext = comments.size() > limit;

        if (hasNext) {
            comments.remove(limit);
        }

        Integer nextCursor = null;

        // 다음 요청에 사용할 커서
        if (hasNext) {
            nextCursor = comments.get(
                    comments.size() - 1
            ).commentId();
        }

        return new CommentPageResponseDto(
                comments,
                nextCursor,
                hasNext
        );
    }

    // 댓글 수정
    @Transactional
    public void updateComment(
            int userId,
            int commentId,
            CommentRequestDto request) {

        Comment comment = commentFinder.findById(commentId);

        validateCommentOwner(comment, userId);

        comment.update(request.content());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(
            int userId,
            int commentId) {

        Comment comment = commentFinder.findById(commentId);

        validateCommentOwner(comment, userId);

        comment.delete();

        Post post = comment.getPost();

        // 댓글 수 동기화
        postRepository.decreaseCommentCount(post.getPostId());
    }

    private void validateCommentOwner(
            Comment comment,
            int userId) {

        // 작성자만 댓글 수정 및 삭제 가능
        if (comment.getUser().getUserId() != userId) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }
}