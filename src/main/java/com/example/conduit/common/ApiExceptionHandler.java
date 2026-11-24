package com.example.conduit.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.http.ResponseEntity;

import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {

    return body(e.getStatus(), e.getErrors());
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException e) {

    return body(400, Map.of("body", List.of("is not valid JSON")));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicate(DataIntegrityViolationException e) {

    String message = String.valueOf(e.getMostSpecificCause().getMessage()).toLowerCase();

    String field = message.contains("email") ? "email" : "username";

    return body(409, Map.of(field, List.of("has already been taken")));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoResourceFoundException e) {

    return body(404, Map.of("route", List.of("not found")));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception e) {

    logger.error("Unhandled exception", e);

    return body(500, Map.of("server", List.of("internal server error")));
  }

  private ResponseEntity<Map<String, Object>> body(int status, Map<String, List<String>> errors) {

    Map<String, Object> body = new LinkedHashMap<>();

    body.put("errors", errors);

    return ResponseEntity.status(status).body(body);
  }
}
