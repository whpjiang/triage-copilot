package com.example.triagecopilot.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Invalid request");
        return ApiResponse.fail(msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<String> handleBindException(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Invalid request");
        return ApiResponse.fail(msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ApiResponse.fail("Invalid JSON request body");
    }

    @ExceptionHandler(Throwable.class)
    public ApiResponse<String> handleThrowable(Throwable ex, HttpServletRequest request) {
        return ApiResponse.fail("Unhandled exception at " + request.getRequestURI() + ": " + ex.getMessage());
    }
}
