package com.dbt.chatease.Utils;

import com.dbt.chatease.Exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.MailException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business exceptions.
     */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());
        return Result.fail(e.getMessage());
    }

    /**
     * Handles illegal argument and validation exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal Argument Exception: {}", e.getMessage());
        //For validation errors, usually return the specific message
        return Result.fail(e.getMessage());
    }

    /**
     * Handles email sending exceptions (MailException).
     */
    @ExceptionHandler(MailException.class)
    public Result handleMailException(MailException e) {
        log.error("Mail Sending Exception: {}", e.getMessage(), e);
        //This is a service failure, inform the user to retry.
        return Result.fail("Email sending failed, please try again later.");
    }

    /**
     * Handles data access exceptions (e.g., database connection issues).
     */
    @ExceptionHandler(DataAccessException.class)
    public Result  handleDataAccessException(DataAccessException e) {
        log.error("Data Access Exception: {}", e.getMessage(), e);
        //For DB/system issues
        return Result.fail("System busy, please try again later.");
    }

    /**
     * Handles general runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error("💥 General Runtime Exception: {}", e.getMessage(), e);
        return Result.fail("System error, please try again later.");
    }

    /**
     * Handles all uncaught exceptions.
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        log.error("Unknown System Exception: {}", e.getMessage(), e);
        return Result.fail("System busy, please try again later.");
    }

    /**
     * Handles validation exceptions for method arguments.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationExceptions(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation Exception: {}", errorMessage);
        return Result.fail(errorMessage);
    }
}