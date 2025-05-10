package com.capstone.group46.pronounceit.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map requests starting with /uploads/ to the uploads directory
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:uploads/");

        // Map requests starting with /images/ to the images directory
        registry
            .addResourceHandler("/images/**")
            .addResourceLocations("file:backend/src/main/resources/images/");

        // Map requests starting with /audio/ to the audio directory
        registry
            .addResourceHandler("/audio/**")
            .addResourceLocations("file:backend/src/main/resources/audio/");
    }
}
