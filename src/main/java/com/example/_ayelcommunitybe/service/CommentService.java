package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.comment.CommentRequestDto;
import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import com.example._ayelcommunitybe.entity.Comment;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.CommentRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 댓글 작성
    @Transactional
    public int createComment(
            int userId,
            int postId,
            CommentRequestDto request
    ) {

        User user = findUser(userId);
        Post post = findPost(postId);

        Comment comment = new Comment(
                user,
                post,
                request.content()
        );

        Comment savedComment = commentRepository.save(comment);

        // 게시글 댓글 수 증가
        post.increaseCommentCount();

        return savedComment.getCommentId();
    }

    // 댓글 목록 조회
    public List<CommentResponseDto> getComments(
            int postId
    ) {

        return commentRepository
                .findByPost_PostIdAndDeletedAtIsNull(postId)
                .stream()
                .map(comment -> new CommentResponseDto(
                        comment.getCommentId(),
                        comment.getContent(),
                        comment.getUser().getNickname(),
                        comment.getCreatedAt()
                ))
                .toList();
    }

    // 댓글 수정
    @Transactional
    public void updateComment(
            int userId,
            int commentId,
            CommentRequestDto request
    ) {

        Comment comment = findComment(commentId);

        validateCommentOwner(
                comment,
                userId
        );

        comment.update(
                request.content()
        );
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(
            int userId,
            int commentId
    ) {

        Comment comment = findComment(commentId);

        validateCommentOwner(
                comment,
                userId
        );

        comment.delete();

        Post post = comment.getPost();

        // 게시글 댓글 수 감소
        post.decreaseCommentCount();
    }

    private User findUser(
            int userId
    ) {
        return userRepository
                .findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private Post findPost(
            int postId
    ) {
        return postRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.POST_NOT_FOUND
                        )
                );
    }

    private Comment findComment(
            int commentId
    ) {
        return commentRepository
                .findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.COMMENT_NOT_FOUND
                        )
                );
    }

    private void validateCommentOwner(
            Comment comment,
            int userId
    ) {
        if (comment.getUser().getUserId() != userId) {
            throw new CustomException(
                    ErrorCode.FORBIDDEN
            );
        }
    }
}