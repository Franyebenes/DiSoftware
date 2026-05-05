package edu.esi.ds.esiusuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Esto le dice al servidor que acepte peticiones de tu Angular
        registry.addMapping("/**") 
                .allowedOrigins("http://localhost:4200") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // El OPTIONS arregla el error actual
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}