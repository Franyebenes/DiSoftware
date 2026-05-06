# 📋 PLAN DE ACCIÓN - IMPLEMENTACIÓN DE SEGURIDAD

## Fase 1: CRÍTICO (48 horas)

### Tarea 1.1: Migrar a SQL Server ⏱️ 2 horas

**Paso 1: Actualizar pom.xml**

```xml
<!-- CAMBIOS EN pom.xml -->

<!-- ELIMINAR esta sección -->
<!-- <dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency> -->

<!-- AGREGAR SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.1.jre11</version>
</dependency>

<!-- AGREGAR Jasypt para encriptación -->
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

<!-- AGREGAR Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>

<!-- AGREGAR para validación de email -->
<dependency>
    <groupId>commons-validator</groupId>
    <artifactId>commons-validator</artifactId>
    <version>1.7</version>
</dependency>

<!-- AGREGAR para logging -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>

<!-- AGREGAR para auditoría -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-envers</artifactId>
</dependency>
```

**Paso 2: Crear nueva base de datos SQL Server**

```sql
-- Script para SQL Server
CREATE DATABASE esiusuarios;

-- Usar cifrado transparente de datos (TDE)
ALTER DATABASE esiusuarios SET ENCRYPTION ON;

-- Crear tabla users con encriptación
USE esiusuarios;

CREATE TABLE users (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    email NVARCHAR(255) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    token NVARCHAR(36) NOT NULL UNIQUE,
    confirmed BIT NOT NULL DEFAULT 0,
    reset_token NVARCHAR(36) UNIQUE,
    reset_token_expiry DATETIME2,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Crear índices para seguridad y rendimiento
CREATE NONCLUSTERED INDEX idx_email ON users(email);
CREATE NONCLUSTERED INDEX idx_token ON users(token);
CREATE NONCLUSTERED INDEX idx_reset_token ON users(reset_token);

-- Row-Level Security (RLS) para protección adicional
CREATE TABLE dbo.rls_predicate (
    user_id BIGINT,
    department_id INT
);

-- Audit trail para cambios de contraseña
CREATE TABLE users_audit (
    audit_id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT,
    action NVARCHAR(50),
    changed_at DATETIME2 DEFAULT GETDATE(),
    old_password_hash NVARCHAR(255),
    new_password_hash NVARCHAR(255),
    ip_address NVARCHAR(45)
);
```

**Paso 3: Actualizar application.properties**

```properties
# ===== DATABASE CONFIGURATION =====
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=esiusuarios;encrypt=true;trustServerCertificate=true;loginTimeout=30
spring.datasource.username=sa
spring.datasource.password=Escarabajo1.
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# ===== CONNECTION POOL =====
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ===== JPA/HIBERNATE =====
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.SQLServer2016Dialect
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true

# ===== ENCRYPTION JASYPT =====
jasypt.encryptor.algorithm=PBEWithMD5AndTripleDES
jasypt.encryptor.password=${JASYPT_PASSWORD:MySecretPassword}
jasypt.encryptor.bean=stringEncryptor

# ===== SECURITY =====
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
server.ssl.protocol=TLS
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
server.http2.enabled=true

# ===== HTTPS ONLY =====
server.http.enabled=false
server.error.whitelabel.enabled=false

# ===== LOGGING =====
logging.level.root=WARN
logging.level.edu.esi.ds.esiusuarios=INFO
logging.level.org.springframework.security=DEBUG
logging.file.name=logs/esiusuarios.log
logging.file.max-size=10MB
logging.file.max-history=30
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# ===== AUDIT LOG =====
spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_audit
spring.jpa.properties.org.hibernate.envers.store_data_at_delete=true
```

---

### Tarea 1.2: Implementar Encriptación de Email ⏱️ 1 hora

**Crear EmailEncryptor.java**

```java
package edu.esi.ds.esiusuarios.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class EmailEncryptor implements AttributeConverter<String, String> {
    
    @Autowired
    private BasicTextEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String email) {
        if (email == null) return null;
        try {
            return encryptor.encrypt(email);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando email", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedEmail) {
        if (encryptedEmail == null) return null;
        try {
            return encryptor.decrypt(encryptedEmail);
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando email", e);
        }
    }
}
```

**Actualizar User.java**

```java
package edu.esi.ds.esiusuarios.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Encrypted;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 500)
    @Encrypted  // ← Encriptado
    private String email;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 100)
    @Encrypted  // ← Encriptado
    private String token;

    @Column(nullable = false)
    private Boolean confirmed = false;

    @Column(unique = true, length = 100)
    @Encrypted  // ← Encriptado
    private String resetToken;

    @Column
    private LocalDateTime resetTokenExpiry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Boolean getConfirmed() { return confirmed; }
    public void setConfirmed(Boolean confirmed) { this.confirmed = confirmed; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public LocalDateTime getResetTokenExpiry() { return resetTokenExpiry; }
    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) { 
        this.resetTokenExpiry = resetTokenExpiry; 
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

### Tarea 1.3: Implementar Rate Limiting ⏱️ 1.5 horas

**Crear RateLimitingService.java**

```java
package edu.esi.ds.esiusuarios.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RateLimitingService {
    
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final int DURATION_MINUTES = 15;
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String email) {
        Bucket bucket = buckets.computeIfAbsent(email, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    public long getRemainingTokens(String email) {
        Bucket bucket = buckets.get(email);
        if (bucket != null) {
            return bucket.estimateAbilityToConsume(1).getRoundedTokensToConsume();
        }
        return REQUESTS_PER_MINUTE;
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            REQUESTS_PER_MINUTE, 
            Refill.intervally(REQUESTS_PER_MINUTE, Duration.ofMinutes(DURATION_MINUTES))
        );
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }

    public void reset(String email) {
        buckets.remove(email);
    }
}
```

**Actualizar UserController.java - LOGIN**

```java
@PostMapping("/login")
public String login(@RequestBody Map<String, String> credentials, HttpServletRequest request) {
    JSONObject jsonCredentials = new JSONObject(credentials);
    String email = jsonCredentials.optString("email");
    String password = jsonCredentials.optString("password");
    
    // Rate limiting
    if (!rateLimitingService.isAllowed(email)) {
        logger.warn("LOGIN ATTEMPT BLOCKED - Rate limit exceeded for: {}", email);
        throw new ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS, 
            "Demasiados intentos de login. Intenta más tarde en 15 minutos."
        );
    }
    
    if (email.isEmpty() || password.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email y contraseña requeridos");
    }
    
    String token = this.service.login(email, password);
    if (token == null) {
        logger.warn("LOGIN FAILED - Invalid credentials for: {}", email);
        throw new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, 
            "Email o contraseña incorrectos, o cuenta no confirmada"
        );
    }
    
    logger.info("LOGIN SUCCESS - User: {}, IP: {}", email, request.getRemoteAddr());
    return token;
}
```

---

### Tarea 1.4: Agregar Headers de Seguridad ⏱️ 45 minutos

**Crear SecurityHeadersFilter.java**

```java
package edu.esi.ds.esiusuarios.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class SecurityHeadersFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // OWASP Secure Headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
        httpResponse.setHeader("Content-Security-Policy", "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'");
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        httpResponse.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        chain.doFilter(request, response);
    }
}
```

**Actualizar CorsConfig.java**

```java
package edu.esi.ds.esiusuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://localhost:4200")  // ← HTTPS solo
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization")  // ← Específico
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## Fase 2: ALTO (Próxima semana)

### Tarea 2.1: Mejorar Validación de Input ⏱️ 1.5 horas

**Actualizar UserService.java**

```java
import org.apache.commons.validator.routines.EmailValidator;

public String register(String email, String pwd1) {
    // ✅ Validación mejorada de email (RFC 5322)
    EmailValidator validator = EmailValidator.getInstance();
    if (!validator.isValid(email)) {
        logger.warn("INVALID EMAIL FORMAT: {}", email);
        return null;
    }
    
    // ✅ Longitud máxima
    if (email.length() > 255) {
        logger.warn("EMAIL TOO LONG: {}", email);
        return null;
    }
    
    if (userRepository.findByEmail(email).isPresent()) {
        logger.warn("EMAIL ALREADY REGISTERED: {}", email);
        return null;
    }
    
    // ✅ Contraseña segura
    if (!isPasswordSecure(pwd1)) {
        logger.warn("PASSWORD NOT SECURE for email: {}", email);
        return null;
    }
    
    // Rest del código...
}

private boolean isPasswordSecure(String password) {
    if (password == null || password.length() < 8 || password.length() > 128) {
        return false;
    }
    
    // Más requisitos estrictos
    boolean hasUppercase = password.matches(".*[A-Z].*");
    boolean hasLowercase = password.matches(".*[a-z].*");
    boolean hasDigit = password.matches(".*\\d.*");
    boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
    
    return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
}
```

---

### Tarea 2.2: Verificar CVEs en Dependencias ⏱️ 30 minutos

**En terminal, ejecutar:**

```bash
cd c:\Users\Fran\DSoftware\esiusuarios

# Agregar OWASP Dependency-Check en pom.xml
# Luego ejecutar:
mvn clean verify
mvn org.owasp:dependency-check-maven:check
```

**O agregar en pom.xml:**

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.0</version>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

### Tarea 2.3: Logging de Seguridad ⏱️ 1 hora

**Crear SecurityAuditService.java**

```java
package edu.esi.ds.esiusuarios.security;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

@Service
public class SecurityAuditService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    public void logLoginAttempt(String email, String ipAddress, boolean success) {
        auditLogger.info("LOGIN_ATTEMPT - Email: {}, IP: {}, Success: {}, Timestamp: {}", 
            email, ipAddress, success, LocalDateTime.now());
    }

    public void logPasswordChange(String email, String ipAddress) {
        auditLogger.info("PASSWORD_CHANGED - Email: {}, IP: {}, Timestamp: {}", 
            email, ipAddress, LocalDateTime.now());
    }

    public void logPasswordReset(String email, String ipAddress) {
        auditLogger.info("PASSWORD_RESET - Email: {}, IP: {}, Timestamp: {}", 
            email, ipAddress, LocalDateTime.now());
    }

    public void logAccountDeletion(String email, String ipAddress) {
        auditLogger.info("ACCOUNT_DELETED - Email: {}, IP: {}, Timestamp: {}", 
            email, ipAddress, LocalDateTime.now());
    }

    public void logSuspiciousActivity(String description, String email, String ipAddress) {
        auditLogger.warn("SUSPICIOUS_ACTIVITY - Description: {}, Email: {}, IP: {}, Timestamp: {}", 
            description, email, ipAddress, LocalDateTime.now());
    }
}
```

---

## Fase 3: MEDIO (Próximas 2 semanas)

### Tarea 3.1: Implementar CAPTCHA ⏱️ 2 horas

**En pom.xml:**

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-recaptcha-enterprise</artifactId>
    <version>1.0.0.RELEASE</version>
</dependency>
```

**En UserController - Registro:**

```java
@PostMapping("/register")
public String register(@RequestBody Map<String, String> credentials) {
    JSONObject jsonCredentials = new JSONObject(credentials);
    String email = jsonCredentials.optString("email");
    String pwd1 = jsonCredentials.optString("pwd1");
    String pwd2 = jsonCredentials.optString("pwd2");
    String recaptchaToken = jsonCredentials.optString("recaptchaToken");
    
    // Validar CAPTCHA
    if (!recaptchaService.validateToken(recaptchaToken)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CAPTCHA inválido");
    }
    
    // Rest del código...
}
```

---

### Tarea 3.2: Implementar MFA (Multi-Factor Authentication) ⏱️ 3 horas

**En pom.xml:**

```xml
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>
```

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Migración a SQL Server completada
- [ ] Jasypt configurado para encriptación
- [ ] Email encriptado en BD
- [ ] Rate limiting funcional
- [ ] Headers de seguridad agregados
- [ ] Validación de email mejorada
- [ ] CVE dependency-check ejecutado
- [ ] Logging de seguridad implementado
- [ ] HTTPS/SSL configurado
- [ ] Pruebas de penetración completadas
- [ ] OWASP ZAP escaneo limpio
- [ ] CAPTCHA en registro funcionando
- [ ] MFA implementado
- [ ] Documentación actualizada
- [ ] Equipo capacitado en seguridad

---

## 🚀 COMANDO PARA INICIAR IMPLEMENTACIÓN

```bash
cd c:\Users\Fran\DSoftware\esiusuarios

# 1. Actualizar pom.xml
# (Realizar cambios del archivo pom.xml anterior)

# 2. Compilar
mvn clean install

# 3. Ejecutar tests de seguridad
mvn org.owasp:dependency-check-maven:check

# 4. Iniciar aplicación
mvn spring-boot:run
```

---

**Documento creado:** 6 de Mayo de 2026  
**Próxima revisión:** Después de cada fase completada
