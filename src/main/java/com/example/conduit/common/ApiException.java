package com.example.conduit.common;

import java.util.List;

import java.util.Map;

public class ApiException extends RuntimeException {

  private final int status;

  private final Map<String, List<String>> errors;

  public ApiException(int status, Map<String, List<String>> errors) {

    super("API error " + status);

    this.status = status;

    this.errors = errors;
  }

  public int getStatus() {
    return status;
  }

  public Map<String, List<String>> getErrors() {
    return errors;
  }
}
