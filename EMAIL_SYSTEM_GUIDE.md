# Email Sistemi ve Randevu Yönetimi - Kurulum ve Kullanım Kılavuzu

## 🎉 Başarıyla Eklenen Özellikler

### ✅ 1. Email Doğrulama (Email Verification)
- ✅ Kullanıcı kaydı sonrası otomatik doğrulama email'i gönderimi
- ✅ Email doğrulanmadan giriş yapılamama
- ✅ Doğrulama linki (24 saat geçerli)
- ✅ Doğrulama email'i yeniden gönderme

### ✅ 2. Şifre Sıfırlama (Password Reset)
- ✅ "Şifremi Unuttum" özelliği
- ✅ Email ile reset linki gönderimi (1 saat geçerli)
- ✅ Güvenli şifre sıfırlama

### ✅ 3. Randevu Bildirimleri
- ✅ Randevu onay email'i
- ✅ Randevu hatırlatma email'i (24 saat öncesinden otomatik)
- ✅ Randevu iptal bildirimi

---

## 📋 Yeni API Endpoints

### Authentication Endpoints

#### 1. Kullanıcı Kaydı (Register)
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "phone": "+90 555 123 4567"
}
```
**Yanıt:** Access token + Refresh token + Verification email gönderilir

#### 2. Email Doğrulama
```http
GET /api/auth/verify-email?token=your-verification-token
```
**Yanıt:** Email doğrulanır

#### 3. Doğrulama Email'i Yeniden Gönder
```http
POST /api/auth/resend-verification?email=user@example.com
```

#### 4. Giriş (Login)
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```
**Not:** Email doğrulanmadıysa giriş yapılamaz!

#### 5. Şifremi Unuttum (Forgot Password)
```http
POST /api/auth/forgot-password?email=user@example.com
```
**Yanıt:** Password reset email gönderilir

#### 6. Şifre Sıfırlama (Reset Password)
```http
POST /api/auth/reset-password?token=reset-token&newPassword=NewPass123
```

---

## ⚙️ Email Konfigürasyonu

### application.yml Ayarları

Aşağıdaki ayarlar yapıldı:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:your-email@gmail.com}
    password: ${MAIL_PASSWORD:your-app-password}

app:
  url: ${APP_URL:http://localhost:8080}
  email:
    from: ${MAIL_FROM:noreply@appointment.com}
```

### 🔐 Gmail Kullanımı İçin Gerekli Ayarlar

1. **Gmail App Password Oluşturma:**
   - Google Account → Security
   - 2-Step Verification'ı aktif et
   - App passwords → Mail → Generate
   - Oluşturulan şifreyi kopyala

2. **Environment Variables Ayarlama:**

**Windows (PowerShell):**
```powershell
$env:MAIL_USERNAME="your-email@gmail.com"
$env:MAIL_PASSWORD="your-16-digit-app-password"
$env:APP_URL="http://localhost:8080"
```

**Linux/Mac:**
```bash
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-16-digit-app-password"
export APP_URL="http://localhost:8080"
```

---

## 🗄️ Database Migrations

3 yeni migration dosyası eklendi:

1. **V1__init_schema.sql** - Users tablosu (Mevcut)
2. **V2__add_email_verification_and_password_reset.sql** - Email verification kolonları
3. **V3__create_appointments_table.sql** - Appointments tablosu

### Yeni Users Kolonları:
- `email_verified` - Email doğrulama durumu
- `verification_token` - Email doğrulama token'ı
- `verification_token_expires_at` - Token geçerlilik süresi
- `password_reset_token` - Şifre sıfırlama token'ı
- `password_reset_token_expires_at` - Reset token geçerlilik süresi

---

## 🎯 Randevu Sistemi (Appointment System)

### Appointment Entity Özellikleri:
- Kullanıcı (user_id)
- Servis Sağlayıcı (service_provider_id)
- Randevu tarihi ve saati
- Süre (dakika cinsinden)
- Servis tipi
- Notlar
- Durum (PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)
- İptal nedeni
- Hatırlatma gönderildi mi?

### Otomatik Hatırlatma Sistemi:
- **Her saat başı çalışır** (Scheduled task)
- 24 saat içinde olan randevular için
- Sadece CONFIRMED durumundaki randevular için
- Henüz hatırlatma gönderilmemiş randevular için

---

## 📧 Email Template'leri

5 farklı email template'i oluşturuldu:

1. **email-verification.html** - Email doğrulama (Yeşil tema)
2. **password-reset.html** - Şifre sıfırlama (Turuncu tema)
3. **appointment-confirmation.html** - Randevu onayı (Mavi tema)
4. **appointment-reminder.html** - Randevu hatırlatma (Sarı tema)
5. **appointment-cancellation.html** - Randevu iptali (Kırmızı tema)

Tüm template'ler **responsive** ve **modern** tasarıma sahip!

---

## 🚀 Çalıştırma

### 1. Email Ayarlarını Yap
Environment variable'ları yukarıdaki gibi ayarla

### 2. Uygulamayı Başlat
```bash
mvn spring-boot:run
```

### 3. Database Migration Otomatik Çalışacak
Flyway otomatik olarak yeni kolonları ve tabloları oluşturacak

---

## 🧪 Test Senaryoları

### Test 1: Kullanıcı Kaydı ve Email Doğrulama
1. POST /api/auth/register ile kayıt ol
2. Email'ini kontrol et (verification link)
3. Link'e tıkla veya GET /api/auth/verify-email?token=xxx
4. POST /api/auth/login ile giriş yap ✅

### Test 2: Doğrulama Yapmadan Giriş Dene
1. Register ol
2. Email doğrulamadan login dene
3. **Beklenen:** "Email not verified" hatası ❌

### Test 3: Şifre Sıfırlama
1. POST /api/auth/forgot-password?email=xxx
2. Email'ini kontrol et (reset link)
3. POST /api/auth/reset-password?token=xxx&newPassword=yyy
4. Yeni şifre ile login yap ✅

---

## ⚠️ Önemli Notlar

1. **Gmail SMTP Limitleri:**
   - Günlük 500 email limiti var
   - Production'da SendGrid, AWS SES veya başka bir servis kullanın

2. **Token Geçerlilik Süreleri:**
   - Email verification: 24 saat
   - Password reset: 1 saat
   - JWT access token: 15 dakika
   - JWT refresh token: 7 gün

3. **Hatırlatma Sistemi:**
   - Scheduler her saat başı çalışır
   - 24 saat içindeki randevular için hatırlatma gönderir
   - Test için zamanı değiştirebilirsiniz (application.yml)

4. **Email Gönderimi Asenkron:**
   - Email'ler arka planda gönderilir (@Async)
   - API response süresini etkilemez

---

## 🔧 Sorun Giderme

### Email Gönderilmiyor?
1. MAIL_USERNAME ve MAIL_PASSWORD doğru mu kontrol et
2. Gmail App Password kullanıyor musun? (normal şifre değil!)
3. 2-Step Verification açık mı?
4. Console log'lara bak: "Email sent successfully" mesajı var mı?

### Token Expired Hatası?
1. Yeni verification email iste: POST /api/auth/resend-verification
2. Yeni password reset iste: POST /api/auth/forgot-password

### Database Migration Hatası?
```bash
# Migration'ları sıfırla (DİKKAT: Tüm veriyi siler!)
mvn flyway:clean
mvn flyway:migrate
```

---

## 🎊 Tamamlanan Tüm Özellikler

✅ Email dependency ekleme (Spring Mail + Thymeleaf)  
✅ Email configuration (SMTP settings)  
✅ EmailService sınıfı (5 farklı email tipi)  
✅ User entity güncelleme (verification & reset tokens)  
✅ Database migration (V2 & V3)  
✅ Email verification flow (register, verify, resend)  
✅ Password reset flow (forgot, reset)  
✅ Appointment entity ve repository  
✅ AppointmentService (create, confirm, cancel, complete)  
✅ Otomatik randevu hatırlatma sistemi (Scheduler)  
✅ 5 modern email template'i  
✅ GlobalExceptionHandler güncelleme  
✅ Async ve Scheduling configuration  

**TOPLAM: 8/8 TODO tamamlandı! 🎉**

---

## 📞 İletişim

Herhangi bir sorun veya soru için issue açabilirsiniz!

**Happy Coding! 🚀**

