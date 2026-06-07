package com.example._ayelcommunitybe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                message,
                data
        );
    }

    // 성공 응답 (데이터 없음)
    public static ApiResponse<Void> success(
            String message
    ) {
        return new ApiResponse<>(
                true,
                message,
                null
        );
    }

    // 실패 응답
    public static ApiResponse<Void> fail(
            String message
    ) {
        return new ApiResponse<>(
                false,
                message,
                null
        );
    }
}