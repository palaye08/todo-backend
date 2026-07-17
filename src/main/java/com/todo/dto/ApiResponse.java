package com.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper used across all controllers.
 *
 * @param <T> type of the payload carried in {@code data}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private T data;
    private int code;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .code(200)
                .message("Succès")
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .code(200)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> of(T data, int code, String message) {
        return ApiResponse.<T>builder()
                .data(data)
                .code(code)
                .message(message)
                .build();
    }
}
