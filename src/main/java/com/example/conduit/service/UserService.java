package com.example.conduit.service;

import com.example.conduit.common.ApiException;

import com.example.conduit.entity.User;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.jdbc.support.GeneratedKeyHolder;

import org.springframework.jdbc.support.KeyHolder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.sql.PreparedStatement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.sql.Timestamp;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Locale;

import java.util.Map;

@Service
public class UserService {

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Autowired
  PasswordEncoder passwordEncoder;

  private User mapUser(ResultSet rs, int rowNum) throws SQLException {

    User user = new User();

    user.setId(rs.getLong("id"));

    user.setUsername(rs.getString("username"));

    user.setEmail(rs.getString("email"));

    user.setPassword(rs.getString("password_hash"));

    user.setBio(rs.getString("bio"));

    user.setImage(rs.getString("image"));

    user.setRole(rs.getString("role"));

    Timestamp createdAt = rs.getTimestamp("created_at");

    user.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());

    return user;
  }

  @Transactional
  public User register(String username, String email, String password) {

    username = username == null ? "" : username.trim();

    email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

    password = password == null ? "" : password;

    Map<String, List<String>> errors = new LinkedHashMap<>();

    if (username.isEmpty()) {
      errors.put("username", List.of("can't be blank"));

    } else if (username.length() < 3 || username.length() > 32) {
      errors.put("username", List.of("must be between 3 and 32 characters"));
    }

    if (email.isEmpty()) {
      errors.put("email", List.of("can't be blank"));

    } else if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      errors.put("email", List.of("is invalid"));
    }

    if (password.isEmpty()) {
      errors.put("password", List.of("can't be blank"));

    } else if (password.length() < 8) {
      errors.put("password", List.of("must be at least 8 characters"));

    } else if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
      errors.put("password", List.of("must be at most 72 bytes"));
    }

    if (!errors.isEmpty()) {
      throw new ApiException(422, errors);
    }

    Map<String, List<String>> conflicts = new LinkedHashMap<>();

    if (count("SELECT COUNT(*) FROM users WHERE username = ?", username) > 0) {
      conflicts.put("username", List.of("has already been taken"));
    }

    if (count("SELECT COUNT(*) FROM users WHERE email = ?", email) > 0) {
      conflicts.put("email", List.of("has already been taken"));
    }

    if (!conflicts.isEmpty()) {
      throw new ApiException(409, conflicts);
    }

    String hash = passwordEncoder.encode(password);

    String finalUsername = username;
    String finalEmail = email;

    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {

      PreparedStatement ps = connection.prepareStatement(
          "INSERT INTO users (username, email, password_hash) VALUES (?, ?, ?)",
          new String[]{"id"});

      ps.setString(1, finalUsername);

      ps.setString(2, finalEmail);

      ps.setString(3, hash);

      return ps;
    }, keyHolder);

    return getById(keyHolder.getKey().longValue());
  }

  public User signin(String email, String password) {

    email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

    password = password == null ? "" : password;

    User user = findByEmail(email);

    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
      throw new ApiException(401, Map.of("email or password", List.of("is invalid")));
    }

    return user;
  }

  public User getById(long id) {

    List<User> users = jdbcTemplate.query(
        "SELECT id, username, email, password_hash, bio, image, role, created_at FROM users WHERE id = ?",
        this::mapUser, id);

    return users.isEmpty() ? null : users.get(0);
  }

  public User findByEmail(String email) {

    List<User> users = jdbcTemplate.query(
        "SELECT id, username, email, password_hash, bio, image, role, created_at FROM users WHERE email = ?",
        this::mapUser, email);

    return users.isEmpty() ? null : users.get(0);
  }

  public List<User> getUsers() {

    return jdbcTemplate.query(
        "SELECT id, username, email, password_hash, bio, image, role, created_at FROM users ORDER BY id",
        this::mapUser);
  }

  @Transactional
  public User update(long id, String username, String email, String password, String bio, String image) {

    User user = getById(id);

    if (user == null) {
      throw new ApiException(401, Map.of("token", List.of("is invalid")));
    }

    Map<String, List<String>> errors = new LinkedHashMap<>();

    if (username != null) {
      username = username.trim();
      if (username.length() < 3 || username.length() > 32) {
        errors.put("username", List.of("must be between 3 and 32 characters"));
      } else if (count("SELECT COUNT(*) FROM users WHERE username = ? AND id <> ?", username, id) > 0) {
        errors.put("username", List.of("has already been taken"));
      }
    }

    if (email != null) {
      email = email.trim().toLowerCase(Locale.ROOT);
      if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
        errors.put("email", List.of("is invalid"));
      } else if (count("SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?", email, id) > 0) {
        errors.put("email", List.of("has already been taken"));
      }
    }

    if (password != null && !password.isBlank()) {
      if (password.length() < 8) {
        errors.put("password", List.of("must be at least 8 characters"));
      } else if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
        errors.put("password", List.of("must be at most 72 bytes"));
      }
    }

    if (!errors.isEmpty()) {
      throw new ApiException(422, errors);
    }

    if (username != null) {
      jdbcTemplate.update("UPDATE users SET username = ? WHERE id = ?", username, id);
    }
    if (email != null) {
      jdbcTemplate.update("UPDATE users SET email = ? WHERE id = ?", email, id);
    }
    if (password != null && !password.isBlank()) {
      jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?",
          passwordEncoder.encode(password), id);
    }
    if (bio != null) {
      jdbcTemplate.update("UPDATE users SET bio = ? WHERE id = ?", bio, id);
    }
    if (image != null) {
      jdbcTemplate.update("UPDATE users SET image = ? WHERE id = ?", image, id);
    }

    return getById(id);
  }

  private int count(String sql, Object... args) {

    Integer n = jdbcTemplate.queryForObject(sql, Integer.class, args);

    return n == null ? 0 : n;
  }
}
