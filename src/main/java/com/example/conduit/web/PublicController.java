package com.example.conduit.web;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import java.util.Map;

@RestController

@RequestMapping("/api")
public class PublicController {

  @GetMapping("/health")
  public Map<String, Object> health() {

    return Map.of("ok", true);
  }

  @GetMapping("/tags")
  public Map<String, Object> tags() {

    return Map.of("tags", List.of());
  }

  @GetMapping("/articles")
  public Map<String, Object> articles() {

    return Map.of("articles", List.of(), "articlesCount", 0);
  }
}
