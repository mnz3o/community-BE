package com.example._ayelcommunitybe.exception;

import lombok.Getter;

@Getter
public class CustomException
        extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(
            ErrorCode errorCode
    ) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}