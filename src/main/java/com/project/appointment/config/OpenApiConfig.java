package com.project.appointment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Appointment Management System API")
                        .version("1.0.0")
                        .description("""
                                Bu API, randevu yönetim sistemi için tüm endpoint'leri sağlar.
                                
                                ## Özellikler
                                - 🔐 JWT tabanlı kimlik doğrulama
                                - 👥 Kullanıcı ve işletme yönetimi
                                - 📅 Randevu oluşturma ve yönetimi
                                - 👨‍💼 Personel ve mesai saatleri yönetimi
                                - ⭐ Değerlendirme sistemi
                                - 🔔 Bildirim sistemi
                                - ❤️ Favori işletmeler
                                - 📊 Dashboard ve analitik
                                
                                ## Kimlik Doğrulama
                                API'ye erişmek için JWT token gereklidir. Token almak için:
                                1. `/api/auth/register` ile kayıt olun
                                2. Email doğrulaması yapın
                                3. `/api/auth/login` ile giriş yapın
                                4. Dönen `accessToken`'ı "Authorize" butonuna girin
                                """)
                        .contact(new Contact()
                                .name("HLT0 Rezervasyon")
                                .email("hlt0rezervasyon@gmail.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description("Ana Sunucu")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token'ınızı buraya girin (Bearer prefix olmadan)")));
    }
}





