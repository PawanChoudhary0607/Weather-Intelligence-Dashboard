package com.portfolio.weatherintel.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Catches any exception not already handled within
 * {@code DashboardController} and renders a friendly error page instead of
 * letting Spring Boot's default whitelabel error page (or a raw stack
 * trace) reach the browser.
 *
 * {@code DashboardController} already handles the expected,
 * recoverable failure modes (provider unavailable, location not found)
 * inline so the dashboard can degrade gracefully with specific messaging.
 * This handler is the safety net for anything unexpected.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleUnexpectedException(Exception ex, Model model) {
        log.error("Unhandled exception while serving request", ex);
        model.addAttribute("errorMessage",
                "Something unexpected happened on our end. Please try again in a moment.");
        return "error";
    }
}
