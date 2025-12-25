# 🔐 Güvenlik Yapılandırması

## Environment Variables Kurulumu

Bu proje hassas bilgileri `.env` dosyası ile yönetir. Aşağıdaki adımları takip edin:

### 1. `.env` Dosyası Oluşturma

Proje kök dizininde `.env` dosyası oluşturun:

```bash
cp .env.example .env
```

### 2. Değerleri Güncelleme

`.env` dosyasını açın ve değerleri kendi bilgilerinizle güncelleyin:

#### Database Configuration
```env
DB_URL=jdbc:mysql://localhost:3306/appointment_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=your_database_username
DB_PASSWORD=your_secure_password
```

#### JWT Configuration
```env
# ÖNEMLİ: Güvenli bir secret key oluşturun (en az 256 bit)
# Aşağıdaki komutu kullanabilirsiniz:
# openssl rand -base64 32
JWT_SECRET=your_secure_jwt_secret_key_here
JWT_ACCESS_TOKEN_EXPIRATION=900000      # 15 dakika
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7 gün
```

#### Email Configuration
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_gmail_app_password  # Gmail App Password kullanın!
```

**Gmail App Password Nasıl Alınır?**
1. Google Hesabınıza gidin
2. Güvenlik > 2 Adımlı Doğrulama'yı aktif edin
3. Güvenlik > Uygulama şifreleri'ne gidin
4. "Uygulama seç" > "Diğer" > "Appointment System" yazın
5. Oluşturulan 16 haneli şifreyi kopyalayın

#### Redis Configuration
```env
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 3. Güvenlik Kontrol Listesi

- [ ] `.env` dosyası `.gitignore`'a eklendi ✅
- [ ] Gerçek şifreler `.env` dosyasında
- [ ] `.env.example` sadece placeholder değerler içeriyor
- [ ] JWT secret en az 256 bit (32 karakter)
- [ ] Gmail App Password kullanılıyor (normal şifre DEĞİL!)
- [ ] Production'da environment variables sunucu seviyesinde ayarlandı

### 4. Production Deployment

Production ortamında `.env` dosyası kullanmayın! Bunun yerine:

**Option 1: Environment Variables (Önerilen)**
```bash
export DB_PASSWORD=your_production_password
export JWT_SECRET=your_production_jwt_secret
# ... diğer değişkenler
```

**Option 2: Docker Secrets**
```yaml
services:
  app:
    environment:
      - DB_PASSWORD_FILE=/run/secrets/db_password
    secrets:
      - db_password
```

**Option 3: Cloud Provider Secrets Manager**
- AWS: AWS Secrets Manager
- Azure: Azure Key Vault
- GCP: Secret Manager

### 5. Güvenli JWT Secret Oluşturma

**Linux/Mac:**
```bash
openssl rand -base64 32
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

**Online (Güvenli):**
https://generate-secret.vercel.app/32

### 6. Supabase Kullanıyorsanız

Eğer Supabase PostgreSQL kullanacaksanız:

```env
DB_URL=jdbc:postgresql://db.[PROJECT-REF].supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password
```

## ⚠️ Önemli Notlar

1. **Asla `.env` dosyasını Git'e commit etmeyin!**
2. **Production'da güçlü şifreler kullanın**
3. **JWT secret'ı periyodik olarak değiştirin**
4. **Email şifresi için Gmail App Password kullanın**
5. **Database şifrenizi team üyeleriyle güvenli kanallardan paylaşın**

## 🔍 Güvenlik Testi

Uygulamayı başlatmadan önce:

```bash
# .env dosyasının varlığını kontrol edin
ls -la .env

# Git status kontrolü - .env görünmemeli!
git status

# Uygulamayı başlatın
mvn spring-boot:run
```

## 📚 Ek Kaynaklar

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [OWASP Security Best Practices](https://owasp.org/www-project-top-ten/)
- [Gmail App Passwords Guide](https://support.google.com/accounts/answer/185833)

