package no.nav.pto.veilarbfilter.rest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value
            = {Throwable.class})
    protected ResponseEntity<Object> handleThrowable(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "An internal error occurred during routing";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value
            = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgumentException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "The request was either invalid or lacked required parameters.";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value
            = {IllegalStateException.class})
    protected ResponseEntity<Object> handleIllegalStateException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Internal server error";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value
            = {IllegalStateException.class})
    protected ResponseEntity<Object> handleBadGatewayException(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "The request failed against backend service or db";
        return handleExceptionInternal(ex, bodyOfResponse,
                new HttpHeaders(), HttpStatus.BAD_GATEWAY, request);
    }
}
