package com.capstone.group46.pronounceit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*") // Accepts any origin, including null
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization"); // Explicitly expose Authorization
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Serve images from /app/uploads/images/ at /images/**
                registry.addResourceHandler("/images/**")
                        .addResourceLocations("file:/app/uploads/images/")
                        .setCachePeriod(3600); // Cache for 1 hour

                // Serve audio from /app/uploads/audio/ at /audio/**
                registry.addResourceHandler("/audio/**")
                        .addResourceLocations("file:/app/uploads/audio/")
                        .setCachePeriod(3600); // Cache for 1 hour

                // Serve badges from /app/uploads/images/badges/ at /images/badges/**
                registry.addResourceHandler("/images/badges/**")
                        .addResourceLocations("file:/app/uploads/images/badges/")
                        .setCachePeriod(3600); // Cache for 1 hour
            }
        };
    }
}