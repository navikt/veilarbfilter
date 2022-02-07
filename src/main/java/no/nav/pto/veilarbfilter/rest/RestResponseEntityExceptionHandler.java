package no.nav.pto.veilarbfilter.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value
            = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgumentException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage() == null ? "The request was either invalid or lacked required parameters." : ex.getMessage();
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value
            = {IllegalStateException.class})
    protected ResponseEntity<Object> handleIllegalStateException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage() == null ? "Internal server error" : ex.getMessage();
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value
            = {HttpServerErrorException.BadGateway.class})
    protected ResponseEntity<Object> handleBadGatewayException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage() == null ? "The request failed against backend service or db" : ex.getMessage();
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
    }

    @ExceptionHandler(value
            = {Throwable.class})
    protected ResponseEntity<Object> handleThrowable(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = ex.getMessage() == null ? "An internal error occurred during routing" : ex.getMessage();
        log.error(ex.getMessage(), ex);
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
