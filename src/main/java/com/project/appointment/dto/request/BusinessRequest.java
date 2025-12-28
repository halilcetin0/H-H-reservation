package com.project.appointment.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessRequest {
    
    @NotBlank(message = "İşletme adı zorunludur")
    @Size(min = 2, max = 255, message = "İşletme adı 2-255 karakter arasında olmalıdır")
    private String name;
    
    @Size(max = 5000, message = "Açıklama 5000 karakteri geçemez")
    private String description;
    
    @NotBlank(message = "Kategori zorunludur")
    private String category;
    
    @NotBlank(message = "Adres zorunludur")
    @Size(max = 500, message = "Adres 500 karakteri geçemez")
    private String address;
    
    @NotBlank(message = "Şehir zorunludur")
    private String city;
    
    @NotBlank(message = "İşletme tipi zorunludur")
    private String businessType;
    
    @NotBlank(message = "Telefon zorunludur")
    @Pattern(regexp = "^[0-9+\\s()-]{10,20}$", message = "Geçersiz telefon numarası formatı")
    private String phone;
    
    @NotBlank(message = "Email zorunludur")
    @Email(message = "Geçersiz email formatı")
    private String email;
    
    private String imageUrl;
}

