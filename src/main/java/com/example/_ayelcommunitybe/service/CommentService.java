package com.example._ayelcommunitybe.service;

import com.example._ayelcommunitybe.dto.comment.CommentPageResponseDto;
import com.example._ayelcommunitybe.dto.comment.CommentRequestDto;
import com.example._ayelcommunitybe.dto.comment.CommentResponseDto;
import com.example._ayelcommunitybe.entity.Comment;
import com.example._ayelcommunitybe.entity.Post;
import com.example._ayelcommunitybe.entity.StoredFile;
import com.example._ayelcommunitybe.entity.User;
import com.example._ayelcommunitybe.exception.CustomException;
import com.example._ayelcommunitybe.exception.ErrorCode;
import com.example._ayelcommunitybe.repository.CommentRepository;
import com.example._ayelcommunitybe.repository.PostRepository;
import com.example._ayelcommunitybe.repository.UserRepository;
import com.example._ayelcommunitybe.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
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
    private final StoredFileRepository storedFileRepository;
    private final MessageSource messageSource;

    // 댓글 작성
    @Transactional
    public int createComment(
            int userId,
            int postId,
            CommentRequestDto request) {

        User user = findUser(userId);
        Post post = findPost(postId);

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
        List<Comment> comments = commentRepository.findComments(
                postId,
                cursor,
                PageRequest.of(0, limit + 1)
        );

        boolean hasNext = comments.size() > limit;

        if (hasNext) {
            comments.remove(limit);
        }

        List<CommentResponseDto> commentResponses = comments.stream()
                .map(comment -> {
                    User user = comment.getUser();
                    boolean isDeleted = user.getDeletedAt() != null;
                    String nickname = isDeleted ? messageSource.getMessage("user.deleted.nickname", null, LocaleContextHolder.getLocale()) : user.getNickname();
                    String profileFileUrl = null;
                    if (!isDeleted) {
                        profileFileUrl = storedFileRepository.findByUserAndIsActiveTrue(user)
                                .map(StoredFile::getFileUrl)
                                .orElse(null);
                    }
                    return new CommentResponseDto(
                            comment.getCommentId(),
                            comment.getUser().getUserId(),
                            comment.getContent(),
                            nickname,
                            profileFileUrl,
                            comment.getCreatedAt()
                    );
                })
                .toList();

        // 다음 요청에 사용할 커서
        Integer nextCursor = null;

        if (hasNext) {
            nextCursor = commentResponses.get(
                    commentResponses.size() - 1
            ).commentId();
        }

        return new CommentPageResponseDto(
                commentResponses,
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

        Comment comment = findComment(commentId);

        validateCommentOwner(comment, userId);

        comment.update(request.content());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(
            int userId,
            int commentId) {

        Comment comment = findComment(commentId);

        validateCommentOwner(comment, userId);

        comment.delete();

        Post post = comment.getPost();

        // 댓글 수 동기화
        postRepository.decreaseCommentCount(post.getPostId());
    }

    private User findUser(int userId) {
        return userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Post findPost(int postId) {
        return postRepository.findByPostIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment findComment(int commentId) {
        return commentRepository.findByCommentIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.COMMENT_NOT_FOUND));
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