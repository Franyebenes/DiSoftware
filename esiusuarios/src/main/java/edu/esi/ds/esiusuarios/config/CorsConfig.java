package edu.esi.ds.esiusuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Configuración CORS más segura
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:4200", "https://localhost:4200")  // Permitir HTTP y HTTPS
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")  // Permitir todos los headers
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight por 1 hora
    }
}