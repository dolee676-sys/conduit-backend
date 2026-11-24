package com.example.conduit.web;

import com.example.conduit.common.ApiException;

import com.example.conduit.entity.User;

import com.example.conduit.service.JwtService;
import com.example.conduit.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;

@RestController

@RequestMapping("/api")
public class AuthController {

  @Autowired
  UserService userService;

  @Autowired
  JwtService jwtService;

  @PostMapping("/users")
  public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest request) {

    if (request == null || request.user == null) {
      throw new ApiException(422, Map.of("body", List.of("is invalid")));
    }

    User user = userService.register(request.user.username, request.user.email, request.user.password);

    String token = jwtService.createToken(user.getId());

    return ResponseEntity.status(HttpStatus.CREATED)

        .body(Map.of("user", toUserResponse(user, token)));
  }

  @PostMapping("/users/login")
  public Map<String, Object> login(@RequestBody LoginRequest request) {

    if (request == null || request.user == null) {
      throw new ApiException(422, Map.of("body", List.of("is invalid")));
    }

    User user = userService.signin(request.user.email, request.user.password);

    String token = jwtService.createToken(user.getId());

    return Map.of("user", toUserResponse(user, token));
  }

  @GetMapping("/user")
  public Map<String, Object> currentUser(@RequestAttribute("currentUser") User user,
                                         @RequestHeader("Authorization") String authorization) {

    String token = authorization.substring("Token ".length()).trim();

    return Map.of("user", toUserResponse(user, token));
  }

  @PutMapping("/user")
  public Map<String, Object> updateUser(@RequestAttribute("currentUser") User user,
                                        @RequestHeader("Authorization") String authorization,
                                        @RequestBody UpdateRequest request) {

    if (request == null || request.user == null) {
      throw new ApiException(422, Map.of("body", List.of("is invalid")));
    }

    User updated = userService.update(
        user.getId(),
        request.user.username,
        request.user.email,
        request.user.password,
        request.user.bio,
        request.user.image);

    String token = authorization.substring("Token ".length()).trim();

    return Map.of("user", toUserResponse(updated, token));
  }

  @GetMapping("/admin/users")
  public Map<String, Object> adminUsers() {

    List<Map<String, Object>> users = userService.getUsers().stream()
        .map(this::toAdminUser)
        .toList();

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("users", users);
    return response;
  }

  private Map<String, Object> toUserResponse(User user, String token) {

    Map<String, Object> map = new LinkedHashMap<>();

    map.put("email", user.getEmail());

    map.put("token", token);

    map.put("username", user.getUsername());

    map.put("bio", user.getBio());

    map.put("image", user.getImage());
    return map;
  }

  private Map<String, Object> toAdminUser(User user) {
    Map<String, Object> map = new LinkedHashMap<>();

    map.put("id", user.getId());

    map.put("username", user.getUsername());

    map.put("email", user.getEmail());

    map.put("bio", user.getBio());

    map.put("image", user.getImage());

    map.put("role", user.getRole().toLowerCase(Locale.ROOT));

    map.put("createdAt", user.getCreatedAt() == null
        ? null
        : user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    return map;
  }

  public static class RegisterRequest {
    public UserData user;
  }

  public static class LoginRequest {
    public LoginData user;
  }

  public static class UpdateRequest {
    public UpdateData user;
  }

  public static class UserData {
    public String username;
    public String email;
    public String password;
  }

  public static class LoginData {
    public String email;
    public String password;
  }

  public static class UpdateData {
    public String username;
    public String email;
    public String password;
    public String bio;
    public String image;
  }
}
