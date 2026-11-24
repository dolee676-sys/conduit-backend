package com.example.conduit.web;

import com.example.conduit.entity.User;
import com.example.conduit.service.JwtService;
import com.example.conduit.service.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;

import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Order(1)

@Component
public class JwtInterceptor implements HandlerInterceptor {

  @Autowired
  JwtService jwtService;

  @Autowired
  UserService userService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {

    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }

    String auth = request.getHeader("Authorization");

    if (auth == null || !auth.startsWith("Token ")) {
      return fail(response, 401, "token", "is missing");
    }

    String token = auth.substring("Token ".length()).trim();

    if (token.isEmpty()) {
      return fail(response, 401, "token", "is missing");
    }

    try {

      long userId = jwtService.parseUserId(token);

      User user = userService.getById(userId);

      if (user == null) {
        return fail(response, 401, "token", "is invalid");
      }

      request.setAttribute("currentUser", user);

      return true;
    } catch (ExpiredJwtException e) {

      return fail(response, 401, "token", "has expired");
    } catch (JwtException | IllegalArgumentException e) {

      return fail(response, 401, "token", "is invalid");
    }
  }

  private boolean fail(HttpServletResponse response, int status, String field, String message)
      throws IOException {

    response.setStatus(status);

    response.setContentType("application/json;charset=UTF-8");

    response.getWriter().write("{\"errors\":{\"" + field + "\":[\"" + message + "\"]}}");

    return false;
  }
}
