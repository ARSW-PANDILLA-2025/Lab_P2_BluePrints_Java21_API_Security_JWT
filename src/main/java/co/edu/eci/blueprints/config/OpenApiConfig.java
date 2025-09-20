package co.edu.eci.blueprints.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
          .info(new Info()
            .title("BluePrints API")
            .version("2.0")
            .description("Parte 2 — Seguridad con JWT (OAuth 2.0)\n\n" +
                        "Esta API implementa autenticación JWT con OAuth2 Resource Server. " +
                        "Para usar los endpoints protegidos:\n\n" +
                        "1. Primero haz login en /auth/login con:\n" +
                        "   - Username: student, Password: student123\n" +
                        "   - Username: assistant, Password: assistant123\n\n" +
                        "2. Copia el access_token de la respuesta\n\n" +
                        "3. Haz clic en 'Authorize' y pega el token (sin 'Bearer ')\n\n" +
                        "Los endpoints /api/* requieren scopes específicos:\n" +
                        "- blueprints.read: para operaciones de lectura (GET)\n" +
                        "- blueprints.write: para operaciones de escritura (POST, PUT, DELETE)")
            .contact(new Contact()
                .name("Escuela Colombiana de Ingeniería")
                .email("soporte@escuelaing.edu.co")
                .url("https://www.escuelaing.edu.co"))
            .license(new License()
                .name("Proyecto Educativo")
                .url("https://github.com/DECSIS-ECI/")))
          .servers(List.of(
              new Server()
                  .url("http://localhost:8080")
                  .description("Servidor de desarrollo local")))
          .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
          .components(new Components().addSecuritySchemes("bearer-jwt",
            new SecurityScheme()
              .name("bearer-jwt")
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT")
              .description("JWT Authorization header using the Bearer scheme. " +
                          "Example: \"Authorization: Bearer {token}\". " +
                          "Obtén tu token usando el endpoint /auth/login.")));
    }
}
