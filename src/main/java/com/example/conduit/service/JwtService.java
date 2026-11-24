package com.example.conduit.service;

import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;

import java.time.Instant;

import java.util.Date;

@Service
public class JwtService {

  @Value("${app.jwt.secret}")
  String secret;

  @Value("${app.jwt.expire-seconds:86400}")
  long expireSeconds;

  private SecretKey key;

  @PostConstruct
  void init() {

    byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);

    if (bytes.length < 32) {
      throw new IllegalStateException(
          "app.jwt.secret must be at least 32 bytes (256 bits); configure JWT_SECRET");
    }

    this.key = Keys.hmacShaKeyFor(bytes);
  }

  public String createToken(long userId) {

    Instant now = Instant.now();

    return Jwts.builder()

        .subject(String.valueOf(userId))

        .issuedAt(Date.from(now))

        .expiration(Date.from(now.plusSeconds(expireSeconds)))

        .signWith(key, Jwts.SIG.HS256)

        .compact();
  }

  public long parseUserId(String token) {

    Claims claims = Jwts.parser()

        .verifyWith(key)

        .build()

        .parseSignedClaims(token)

        .getPayload();

    return Long.parseLong(claims.getSubject());
  }
}
