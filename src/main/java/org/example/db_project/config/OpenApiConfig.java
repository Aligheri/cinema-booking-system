package org.example.db_project.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI cinemaOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");
        Contact contact = new Contact();
        contact.setName("Cinema Booking System");
        Info info = new Info()
                .title("Cinema Booking System API")
                .version("1.0.0")
                .description("A comprehensive Spring Boot backend application for a cinema booking system, " +
                             "demonstrating advanced database design and SQL knowledge. " +
                             "\n\n**Key Features:**\n" +
                             "- User management with role-based access\n" +
                             "- Movie catalog with full-text search\n" +
                             "- Session scheduling with conflict detection\n" +
                             "- Multi-seat booking with pessimistic locking\n" +
                             "- Dynamic pricing based on seat types\n" +
                             "- Analytics and reporting\n" +
                             "\n**Tech Stack:** Java 21, Spring Boot 3.4.1, PostgreSQL 16, Flyway, Testcontainers")
                .contact(contact)
                .license(new License()
                        .name("Educational Project")
                        .url("https://github.com/cinema-booking"));
        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
