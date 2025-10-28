/*
 * @ {#} GlobalExceptionHandler.java   1.0     14/08/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.edu.iuh.fit.dtos.response.ApiResponse;

import java.util.List;
import java.util.stream.Collectors;

/*
 * @description: Global exception handler for the application
 * @author: Tran Hien Vinh
 * @date:   14/08/2025
 * @version:    1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler  {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> ValidationError.builder()
                        .field(fieldError.getField())
                        .rejectedValue(fieldError.getRejectedValue())
                        .message(fieldError.getDefaultMessage())
                        .code("INVALID_" + fieldError.getField().toUpperCase())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity
                .badRequest().
                body(ApiResponse.validationError("Validation failed", errors));
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleAlreadyExists(AlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value(), "CONFLICT_ERROR"));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value(), "NOT_FOUND"));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenRefreshException(TokenRefreshException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value(), "NOT_FOUND"));
    }

    @ExceptionHandler(OAuth2AuthenticationProcessingException.class)
    public ResponseEntity<ApiResponse<Object>> handleOAuth2AuthenticationProcessingException(OAuth2AuthenticationProcessingException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.unauthorized(ex.getMessage()));
    }

    @ExceptionHandler(TokenForgetPasswordException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenForgetPasswordException(TokenForgetPasswordException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(MissingTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleMissingTokenException(MissingTokenException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(FirebaseAuthCustomsException.class)
    public ResponseEntity<ApiResponse<Object>> handleFirebaseAuthException(FirebaseAuthCustomsException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(VerificationCodeException.class)
    public ResponseEntity<ApiResponse<Object>> handleVerificationCodeException(VerificationCodeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(NotFoundEnumValueException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFoundEnumValueException(NotFoundEnumValueException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Object>> handleInsufficientStockException(InsufficientStockException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(CartLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleCartLimitExceededException(CartLimitExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }
    
    @ExceptionHandler(DuplicateAddressException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateAddressException(DuplicateAddressException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaymentException(PaymentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ApiResponse<Object>> handleOrderException(OrderException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(PaypalException.class)
    public ResponseEntity<ApiResponse<Object>> handlePaypalException(PaypalException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }

    @ExceptionHandler(NotActiveException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotActiveException(NotActiveException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value(), "BAD_REQUEST"));
    }
}
