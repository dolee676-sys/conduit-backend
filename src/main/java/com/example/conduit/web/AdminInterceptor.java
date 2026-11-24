package com.example.conduit.web;

import com.example.conduit.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Order(2)

@Component
public class AdminInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    User user = (User) request.getAttribute("currentUser");

    if (user == null || !"ADMIN".equals(user.getRole())) {
      fail(response);
      return false;
    }

    return true;
  }

  private void fail(HttpServletResponse response) throws IOException {

    response.setStatus(403);

    response.setContentType("application/json;charset=UTF-8");

    response.getWriter().write("{\"errors\":{\"resource\":[\"forbidden\"]}}");
  }
}
