# 🔒 INFORME DE SEGURIDAD - SISTEMA DE REGISTRO DE USUARIOS

**Fecha de Auditoría:** Mayo 6, 2026  
**Sistema Analizado:** Microservicio esiusuarios (Puerto 8081)  
**Estado General:** ⚠️ **CRÍTICO - Requiere mejoras urgentes**

---

## 📋 RESUMEN EJECUTIVO

| Requisito | Estado | Prioridad | Observaciones |
|-----------|--------|-----------|---------------|
| ✅ Contraseña robusta (8+ chars, mayús, minús, número, especial) | **IMPLEMENTADO** | ✓ | BCrypt strength 12 |
| ✅ Recuperación con token temporal (1 hora) | **IMPLEMENTADO** | ✓ | UUID único, expiración validada |
| ❌ Análisis OWASP Top 10 | **NO IMPLEMENTADO** | 🔴 CRÍTICO | Falta escaneo de vulnerabilidades |
| ❌ Encriptación de datos sensibles (email) | **PARCIAL** | 🔴 CRÍTICO | Email en texto plano en BD |
| ❌ SQL Server con controles de seguridad | **NO IMPLEMENTADO** | 🔴 CRÍTICO | Usa MySQL, no SQL Server |
| ⚠️ SSL/TLS en conexión BD | **DESHABILITADO** | 🔴 CRÍTICO | `useSSL=false` en properties |
| ✅ CORS configurado | **IMPLEMENTADO** | ✓ | Solo localhost:4200 |
| ✅ Tokens Bearer para API | **IMPLEMENTADO** | ✓ | Header Authorization |
| ❌ Rate limiting (brute force) | **NO IMPLEMENTADO** | 🔴 CRÍTICO | Sin protección contra ataques |
| ❌ Logging de seguridad | **NO IMPLEMENTADO** | 🟠 ALTO | Sin auditoría de intentos fallidos |
| ❌ Validación de input (SQL Injection) | **PARCIAL** | 🟠 ALTO | Solo validación básica |
| ❌ HTTPS obligatorio | **NO IMPLEMENTADO** | 🔴 CRÍTICO | Sin redirección de HTTP a HTTPS |

---

## ✅ PUNTOS FUERTES DETECTADOS

### 1. **Contraseñas Seguras**
```
✓ BCrypt con strength 12 (muy fuerte)
✓ Validación de requisitos: 8+ caracteres, mayúsculas, minúsculas, números, caracteres especiales
✓ Regex completo: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':",.<>?])/
```
**Ubicación:** [esiusuarios/src/main/java/edu/esi/ds/esiusuarios/http/UserController.java](esiusuarios/src/main/java/edu/esi/ds/esiusuarios/http/UserController.java#L151-L161)

### 2. **Recuperación de Contraseña con Token Temporal**
```
✓ Token UUID único generado por solicitud
✓ Expiración en 1 hora (LocalDateTime + 1 HOUR)
✓ Limpieza automática del token después de reset
✓ Validación de expiración antes de permitir cambio
```
**Ubicación:** [esiusuarios/src/main/java/edu/esi/ds/esiusuarios/services/UserService.java](esiusuarios/src/main/java/edu/esi/ds/esiusuarios/services/UserService.java#L114-L165)

### 3. **Gestión de Tokens**
```
✓ UUID únicos para tokens de sesión y confirmación
✓ Bearer tokens en headers HTTP
✓ Validación de confirmación de email antes de login
✓ Tokens únicos por usuario (no reutilizables)
```

### 4. **CORS Restrictivo**
```
✓ Solo permite origen: http://localhost:4200
✓ Métodos limitados: GET, POST, PUT, DELETE, OPTIONS
✓ Credenciales requeridas
```
**Ubicación:** [esiusuarios/src/main/java/edu/esi/ds/esiusuarios/config/CorsConfig.java](esiusuarios/src/main/java/edu/esi/ds/esiusuarios/config/CorsConfig.java)

---

## ❌ PROBLEMAS CRÍTICOS ENCONTRADOS

### 🔴 CRÍTICO 1: SSL/TLS DESHABILITADO EN LA CONEXIÓN A LA BASE DE DATOS

**Problema:**
```properties
# esiusuarios/src/main/resources/application.properties (línea 6)
spring.datasource.url=jdbc:mysql://localhost:3306/esiusuarios?serverTimezone=UTC&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true
```

**Riesgos:**
- ❌ Contraseñas y emails transmitidos SIN CIFRAR en red local
- ❌ Vulnerable a ataques Man-in-the-Middle (MITM)
- ❌ Incumplimiento de estándares de seguridad
- ❌ No cumple OWASP A02:2021 - Cryptographic Failures

**Solución Recomendada:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/esiusuarios?serverTimezone=UTC&autoReconnect=true&useSSL=true&allowPublicKeyRetrieval=false&requireSSL=true
```

---

### 🔴 CRÍTICO 2: BASE DE DATOS INCORRECTA

**Problema:**
- Requisito especifica: **SQL Server**
- Implementación actual: **MySQL** ❌
- Pom.xml: Usa driver MySQL, no SQL Server

**Impacto:**
- No se pueden aplicar controles de seguridad específicos de SQL Server
- Falta configuración de Transparent Data Encryption (TDE)
- Sin Row-Level Security (RLS)
- Sin Always Encrypted

**Solución Recomendada:**
```xml
<!-- Cambiar en pom.xml -->
<!-- Eliminar -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- Agregar SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.1.jre11</version>
</dependency>
```

---

### 🔴 CRÍTICO 3: DATOS SENSIBLES SIN ENCRIPTACIÓN

**Problema:**
- Email almacenado en **TEXTO PLANO** en la base de datos
- Solo passwordHash está encriptado
- Tokens almacenados sin cifrado

**Ubicación:** [esiusuarios/src/main/java/edu/esi/ds/esiusuarios/model/User.java](esiusuarios/src/main/java/edu/esi/ds/esiusuarios/model/User.java#L14-L27)

**Riesgos:**
- ❌ Si la BD es comprometida, emails están expuestos
- ❌ GDPR: Violación de protección de datos personales
- ❌ OWASP A02:2021 - Cryptographic Failures

**Datos que necesitan encriptación:**
```
- email (Datos Personales)
- resetToken (Token sensible)
- token (Token de sesión)
```

---

### 🔴 CRÍTICO 4: SIN ANÁLISIS OWASP TOP 10

**Vulnerabilidades NO verificadas:**

#### A1: Broken Access Control ❌
```
✗ No hay validación de autorización por endpoint
✗ No hay control de acceso granular
✗ No se valida propiedad de recursos
```

#### A2: Cryptographic Failures ❌
```
✗ Email sin cifrar
✗ Datos en tránsito sin TLS en BD
✗ Sin cifrado en reposo
```

#### A3: Injection ⚠️ PARCIAL
```
- Email: Validación básica con regex
- Password: Validación de formato
- PERO: Usando JSONObject sin parameterización en queries
```

#### A4: Insecure Design ❌
```
✗ Sin rate limiting
✗ Sin protección contra fuerza bruta
✗ Sin CAPTCHA en registro
✗ Sin alertas de login sospechoso
```

#### A5: Security Misconfiguration ❌
```
✗ Spring Security no configurado correctamente
✗ CORS demasiado permisivo (allowCredentials=true con *)
✗ Sin headers de seguridad HTTP
```

#### A6: Vulnerable Components ⚠️ DESCONOCIDO
```
? Sin escaneo de CVE en dependencias Maven
? Sin análisis de seguridad de paquetes
```

#### A7: Authentication Failures ⚠️ PARCIAL
```
- Contraseñas fuertes ✓
- Tokens temporales ✓
- PERO: Sin MFA
- PERO: Sin limitación de intentos
- PERO: Sin bloqueo de cuenta
```

#### A8: Data Integrity Failures ❌
```
✗ Sin verificación de integridad de datos
✗ Sin cifrado de extremo a extremo
✗ Sin signatures de tokens
```

#### A9: Logging Failures ❌
```
✗ Sin logs de intentos de login fallidos
✗ Sin auditoría de cambios de contraseña
✗ Sin alertas de actividad sospechosa
```

#### A10: SSRF ✓ BAJO RIESGO
```
✓ No hay llamadas HTTP externas en endpoints sensibles
```

---

### 🔴 CRÍTICO 5: SIN PROTECCIÓN CONTRA FUERZA BRUTA

**Problema:**
- Ningún límite de intentos de login fallidos
- Ningún rate limiting implementado
- Ningún bloqueo de cuenta

**Ataque posible:**
```bash
# Atacante puede hacer 10,000 intentos de login en segundos
curl -X POST http://localhost:8081/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"intento1"}'
```

---

### 🔴 CRÍTICO 6: SIN HEADERS DE SEGURIDAD HTTP

**Falta:**
```
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000
- Content-Security-Policy: ...
- Referrer-Policy: strict-origin-when-cross-origin
```

---

### 🟠 ALTO 7: VALIDACIÓN DE INPUT INCOMPLETA

**Problemas encontrados:**

1. **Email**: Regex muy permisivo
   ```java
   // En UserService.java línea 70
   if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
       // Regex NO valida: TLD, dominios inválidos, caracteres especiales
       // NO cumple RFC 5322
   }
   ```

2. **Sin validación de longitud de input**
   - Email puede ser indefinidamente largo
   - Contraseña puede ser 8 caracteres pero sin máximo

3. **JSONObject.optString() silencioso**
   - Retorna string vacío si no existe
   - Dificulta detección de errores

---

### 🟠 ALTO 8: SIN LOGGING DE SEGURIDAD

**Necesario logging:**
```
- ❌ Intentos de login fallidos
- ❌ Cambios de contraseña
- ❌ Reseteos de contraseña
- ❌ Confirmación de email
- ❌ Eliminación de cuentas
- ❌ Tokens utilizados
```

---

### 🟠 ALTO 9: CORS DEMASIADO PERMISIVO

**Problema:**
```java
// CorsConfig.java
.allowedHeaders("*")  // ⚠️ Acepta cualquier header
.allowCredentials(true)  // ⚠️ Permite cookies con credenciales
```

**Debería ser:**
```java
.allowedHeaders("Content-Type", "Authorization")
.allowedOrigins("https://localhost:4200")  // HTTPS obligatorio
```

---

### 🟡 MEDIO 10: DEPENDENCIAS SIN VERIFICACIÓN CVE

**Pom.xml actual:**
```xml
- spring-boot-starter-parent: 3.3.5 ✓ Reciente
- mysql-connector-j: SIN VERSIÓN ESPECIFICADA ⚠️
- org.json: 20230618 ⚠️ Desactualizado
```

**Sin verificación de:**
- CVEs en dependencias
- Vulnerabilidades conocidas
- Componentes desactualizados

---

## 📋 LISTA DE ACCIONES RECOMENDADAS

### **CRÍTICO (Hacer inmediatamente - < 48 horas)**

- [ ] **1. Cambiar a SQL Server**
  - Reemplazar MySQL por SQL Server driver
  - Configurar Transparent Data Encryption (TDE)
  - Implementar Row-Level Security (RLS)
  - Archivo: `pom.xml`

- [ ] **2. Habilitar SSL/TLS en BD**
  - Cambiar `useSSL=true` en application.properties
  - Generar certificados SSL
  - Validar conexiones seguras
  - Archivo: `application.properties`

- [ ] **3. Encriptar datos sensibles**
  - Email: Implementar cifrado AES-256
  - Tokens: Encriptar en base de datos
  - Usar javax.crypto o Jasypt
  - Archivos: `User.java`, `UserService.java`

- [ ] **4. Implementar Rate Limiting**
  - Max 5 intentos de login en 15 minutos
  - Usar Spring Cloud Resilience4j o Bucket4j
  - Bloqueo temporal de cuenta
  - Archivo: `UserController.java`

- [ ] **5. Agregar Headers de Seguridad**
  - Crear `SecurityHeadersConfig.java`
  - Implementar CustomFilter para headers
  - Validar con OWASP Secure Headers Project

### **ALTO (Hacer en próxima semana - < 1 semana)**

- [ ] **6. Implementar Logging de Seguridad**
  - SLF4J + Logback configurado
  - Auditoría de intentos fallidos
  - Alertas de actividad sospechosa
  - Archivo: `application.properties`

- [ ] **7. Mejorar validación de input**
  - Email: RFC 5322 completo con Apache Commons
  - Longitud máxima de campos
  - Escape de caracteres especiales
  - Archivo: `UserService.java`

- [ ] **8. Verificar CVEs en dependencias**
  - Ejecutar: `mvn clean verify`
  - Usar: OWASP Dependency-Check
  - Actualizar componentes
  - Archivo: `pom.xml`

- [ ] **9. Configurar HTTPS obligatorio**
  - Generar self-signed cert o usar Let's Encrypt
  - Redirect HTTP → HTTPS
  - HSTS header
  - Archivo: `application.properties`

- [ ] **10. Implementar CAPTCHA en registro**
  - Google reCAPTCHA v3
  - Protección contra bots
  - Archivo: `UserController.java`

### **MEDIO (Hacer en próximas 2 semanas)**

- [ ] **11. Multi-Factor Authentication (MFA)**
  - TOTP (Time-based OTP)
  - Email 2FA
  - Archivo: `UserService.java`

- [ ] **12. Implementar JWT correctamente**
  - Usar jjwt library
  - Claims con información validable
  - Expiración clara
  - Refresh tokens

- [ ] **13. Análisis OWASP completo**
  - Usar OWASP ZAP o Burp Suite
  - Penetration testing
  - Reporte detallado

- [ ] **14. Backup y Disaster Recovery**
  - Backups cifrados de BD
  - Plan de recuperación
  - Testeado regularmente

---

## 🔍 CÓDIGO DE EJEMPLO - SOLUCIONES

### Solución 1: Encriptación de Email con Jasypt

```java
// Agregar dependencia en pom.xml
<dependency>
    <groupId>com.github.ulisesbocchio</groupId>
    <artifactId>jasypt-spring-boot-starter</artifactId>
    <version>3.0.5</version>
</dependency>

// En User.java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @Convert(converter = EmailEncryptor.class)  // ← Encriptado
    private String email;

    @Column(nullable = false)
    private String passwordHash;
    
    // ... resto del código
}

// Crear EmailEncryptor.java
@Component
public class EmailEncryptor implements AttributeConverter<String, String> {
    
    @Autowired
    private StringEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String email) {
        return encryptor.encrypt(email);
    }

    @Override
    public String convertToEntityAttribute(String encryptedEmail) {
        return encryptor.decrypt(encryptedEmail);
    }
}
```

### Solución 2: Rate Limiting con Bucket4j

```java
// Agregar dependencia en pom.xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>

// En UserController.java
@RestController
@RequestMapping("/users")
public class UserController {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String email) {
        return cache.computeIfAbsent(email, k -> 
            Bucket4j.builder()
                .addLimit(Limit.of(5, Refill.intervally(5, Duration.ofMinutes(15))))
                .build()
        );
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        
        // Rate limiting
        Bucket bucket = resolveBucket(email);
        if (!bucket.tryConsume(1)) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, 
                "Demasiados intentos de login. Intenta más tarde."
            );
        }
        
        // ... resto del código
    }
}
```

### Solución 3: Headers de Seguridad

```java
// Crear SecurityHeadersConfig.java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
            .requestMatchers("/resources/**", "/static/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .xssProtection()
                    .and()
                .contentSecurityPolicy("default-src 'self'")
                    .and()
                .frameOptions().deny()
                    .and()
                .httpStrictTransportSecurity()
                    .maxAgeInSeconds(31536000)
                    .and()
                .contentTypeOptions().and()
                .referrerPolicy(HeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN);
        
        return http.build();
    }
}
```

### Solución 4: Cambio a SQL Server en pom.xml

```xml
<!-- ANTES: MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>

<!-- DESPUÉS: SQL Server -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.1.jre11</version>
</dependency>
```

### Solución 5: application.properties para SQL Server

```properties
# Base de datos SQL Server con SSL/TLS
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=esiusuarios;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;
spring.datasource.username=sa
spring.datasource.password=TuContraseñaSegura123!
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Seguridad TLS
spring.datasource.hikari.connection-test-query=SELECT 1

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.SQLServer2016Dialect
spring.jpa.properties.hibernate.format_sql=true

# HTTPS
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=TuContraseña
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
server.http2.enabled=true
server.error.whitelabel.enabled=false
```

---

## ✅ CHECKLIST DE VERIFICACIÓN FINAL

Una vez implementadas las soluciones, verificar:

```
SEGURIDAD DE CONTRASEÑAS
- [ ] BCrypt strength 12 funcionando
- [ ] Validación de requisitos completa
- [ ] Hash nunca en logs

RECUPERACIÓN DE CONTRASEÑA
- [ ] Token generado (UUID)
- [ ] Expiración 1 hora validada
- [ ] Email enviado con enlace seguro
- [ ] Token limpiado después de uso
- [ ] No acepta tokens expirados

CRIPTOGRAFÍA
- [ ] Email encriptado en BD
- [ ] Tokens encriptados
- [ ] SSL/TLS en conexión BD (useSSL=true)
- [ ] HTTPS en servidor (443)
- [ ] HSTS header presente

BASE DE DATOS
- [ ] SQL Server configurado
- [ ] TDE habilitado
- [ ] RLS configurado
- [ ] Always Encrypted activado
- [ ] Backups cifrados

PROTECCIÓN CONTRA ATAQUES
- [ ] Rate limiting funciona (5 intentos/15min)
- [ ] Bloqueo temporal después de intentos
- [ ] CAPTCHA en registro
- [ ] Headers de seguridad presentes
- [ ] CORS restrictivo

OWASP TOP 10
- [ ] A1: Broken Access Control - MITIGADO
- [ ] A2: Cryptographic Failures - MITIGADO
- [ ] A3: Injection - MITIGADO
- [ ] A4: Insecure Design - MITIGADO
- [ ] A5: Security Misconfiguration - MITIGADO
- [ ] A6: Vulnerable Components - VERIFICADO
- [ ] A7: Authentication Failures - MITIGADO
- [ ] A8: Data Integrity Failures - MITIGADO
- [ ] A9: Logging Failures - IMPLEMENTADO
- [ ] A10: SSRF - VERIFICADO

AUDITORÍA Y LOGS
- [ ] Intentos fallidos registrados
- [ ] Cambios de contraseña auditados
- [ ] Acceso a cuentas sensibles registrado
- [ ] Alertas configuradas

TESTING
- [ ] Pruebas de penetración completadas
- [ ] OWASP ZAP escaneo limpio
- [ ] CVE dependency-check sin issues
- [ ] Tests de seguridad unitarios
```

---

## 📞 CONTACTO Y SOPORTE

Para dudas sobre la implementación de estas mejoras, consultar:

- **OWASP Top 10:** https://owasp.org/www-project-top-ten/
- **Spring Security:** https://spring.io/guides/gs/securing-web/
- **Jasypt Encryption:** https://github.com/ulisesbocchio/jasypt-spring-boot
- **SQL Server Security:** https://docs.microsoft.com/en-us/sql/relational-databases/security/

---

**Informe generado:** 6 de Mayo de 2026  
**Revisor:** Auditoría de Seguridad Automática  
**Estado:** ⚠️ REQUIERE ACCIÓN INMEDIATA
