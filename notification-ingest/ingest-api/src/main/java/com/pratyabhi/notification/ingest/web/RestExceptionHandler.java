package com.pratyabhi.notification.ingest.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<ProblemDetail> badRequest(BadRequestException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setTitle("Bad Request");
    return ResponseEntity.badRequest().body(pd);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
    String msg =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .orElse("validation failed");
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
    pd.setTitle("Validation Error");
    return ResponseEntity.badRequest().body(pd);
  }
}
