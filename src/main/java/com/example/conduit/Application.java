package com.example.conduit;

import com.example.conduit.web.AdminInterceptor;

import com.example.conduit.web.JwtInterceptor;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class Application {

  @Value("${app.cors.allowed-origin:http://localhost:8080}")
  String allowedOrigin;

  public static void main(String[] args) {

    SpringApplication.run(Application.class, args);
  }

  @Bean
  PasswordEncoder passwordEncoder() {

    return new BCryptPasswordEncoder();
  }

  @Bean
  WebMvcConfigurer createWebMvcConfigurer(@Autowired JwtInterceptor jwtInterceptor,
                                          @Autowired AdminInterceptor adminInterceptor) {

    return new WebMvcConfigurer() {

      @Override
      public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")

            .excludePathPatterns(
                "/api/health",
                "/api/users",
                "/api/users/login",
                "/api/tags",
                "/api/articles");

        registry.addInterceptor(adminInterceptor)
            .addPathPatterns("/api/admin/**");
      }

      @Override
      public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")

            .allowedOrigins(allowedOrigin)

            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")

            .allowedHeaders("Content-Type", "Authorization");
      }
    };
  }
}
