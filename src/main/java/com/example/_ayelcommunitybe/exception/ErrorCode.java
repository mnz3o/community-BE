package com.example._ayelcommunitybe.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 인증
    LOGIN_REQUIRED(
            HttpStatus.UNAUTHORIZED,
            "로그인이 필요합니다."
    ),

    INVALID_LOGIN(
            HttpStatus.BAD_REQUEST,
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    // 회원
    USER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 회원입니다."
    ),

    DUPLICATE_EMAIL(
            HttpStatus.CONFLICT,
            "중복된 이메일입니다."
    ),

    DUPLICATE_NICKNAME(
            HttpStatus.CONFLICT,
            "중복된 닉네임입니다."
    ),

    PASSWORD_MISMATCH(
            HttpStatus.BAD_REQUEST,
            "비밀번호가 다릅니다."
    ),

    // 게시글
    POST_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 게시글입니다."
    ),

    // 댓글
    COMMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 댓글입니다."
    ),
    // 좋아요
    ALREADY_LIKED(
            HttpStatus.BAD_REQUEST,
            "이미 좋아요를 누른 게시글입니다."
    ),

    LIKE_NOT_FOUND(
            HttpStatus.BAD_REQUEST,
            "좋아요가 존재하지 않습니다."
    ),

    // 권한
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "권한이 없습니다."
    ),

    // 파일
    INVALID_FILE_TYPE(
            HttpStatus.BAD_REQUEST,
            "지원하지 않는 파일 형식입니다."
    ),

    FILE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "존재하지 않는 파일입니다."
    );;;

    private final HttpStatus httpStatus;
    private final String message;
}