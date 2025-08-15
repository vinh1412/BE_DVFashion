/*
 * @ {#} ApiResponse.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.exceptions.ErrorDetails;
import vn.edu.iuh.fit.exceptions.ValidationError;

import java.util.List;

/*
 * @description: Generic API response DTO for standardizing API responses across the application
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in the JSON response
public class ApiResponse<T> {
    // Response status
    private boolean success;

    // HTTP status code
    private int statusCode;

    // Response message
    private String message;

    // Main data
    private T data;

    // Error details (only when success = false)
    private ErrorDetails error;

    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message("Operation completed successfully")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(statusCode)
                .message(message)
                .data(data)
                .build();
    }

    // Created response (201)
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(201)
                .message(message)
                .data(data)
                .build();
    }

    // No content response (204)
    public static <T> ApiResponse<T> noContent(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .statusCode(204)
                .message(message)
                .build();
    }

    // Error responses
    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .error(ErrorDetails.builder()
                        .code("ERROR_" + statusCode)
                        .message(message)
                        .build())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, int statusCode, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(statusCode)
                .error(ErrorDetails.builder()
                        .code(errorCode)
                        .message(message)
                        .build())
                .build();
    }

    // Validation error
    public static <T> ApiResponse<T> validationError(String message, List<ValidationError> validationErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(400)
                .error(ErrorDetails.builder()
                        .code("VALIDATION_ERROR")
                        .message(message)
                        .validationErrors(validationErrors)
                        .build())
                .build();
    }

    // UNAUTHORIZED ERROR
    public static <T> ApiResponse<T> unauthorized(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .statusCode(401)
//                .message(message)
                .error(ErrorDetails.builder()
                        .code("UNAUTHORIZED")
                        .message(message)
                        .build())
                .build();
    }

    // Common error responses
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(message, 400, "BAD_REQUEST");
    }

//    public static <T> ApiResponse<T> unauthorized(String message) {
//        return error(message, 401, "UNAUTHORIZED");
//    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return error(message, 403, "FORBIDDEN");
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return error(message, 404, "NOT_FOUND");
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return error(message, 409, "CONFLICT");
    }

    public static <T> ApiResponse<T> internalServerError(String message) {
        return error(message, 500, "INTERNAL_SERVER_ERROR");
    }
}
