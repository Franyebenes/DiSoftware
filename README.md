# DSoftware

Sistema de gestión de entradas para eventos. Arquitectura de microservicios con Spring Boot y Angular.

## Microservicios

### esiusuarios (Puerto 8081)
Servicio de gestión de cuentas de usuario.

**Funcionalidades implementadas:**
-  Registro de usuarios con validación de contraseña segura
-  Login con autenticación BCrypt
-  Confirmación de email
-  Recuperación de contraseña (forgot password)
-  Reset de contraseña con token temporal
-  Cancelación/eliminación de cuenta
-  Validación de tokens para comunicación con otros servicios

**Endpoints:**
- `POST /users/register` - Registro de usuario
- `POST /users/login` - Login
- `GET /users/confirm?token=...` - Confirmación de email
- `POST /users/forgot-password` - Solicitar recuperación de contraseña
- `POST /users/reset-password` - Resetear contraseña
- `DELETE /users/account` - Eliminar cuenta (requiere Bearer token)
- `GET /users/validate-token?token=...` - Validar token para otros servicios
- `GET /external/checkToken/{token}` - Endpoint legacy para compatibilidad

### esientradas (Puerto 8080)
Servicio de gestión de compras de entradas.

**Funcionalidades:**
-  Validación de tokens de usuario con esiusuarios
-  Gestión de compras y pagos con Stripe
-  Generación de PDFs
-  Búsqueda de espectáculos por artista
-  Búsqueda de espectáculos por fecha
-  Comunicación con servicios externos

**Endpoints de búsqueda:**
- `GET /busqueda/getEscenarios` - Lista todos los escenarios
- `GET /busqueda/getEspectaculos?artista=Natos y Waor` - Buscar por artista
- `GET /busqueda/getEspectaculos/{idEscenario}` - Espectáculos de un escenario
- `GET /busqueda/getEspectaculosByFecha?fecha=2026-03-14` - Buscar por fecha

### esife (Puerto 4200)
Frontend Angular para usuarios finales.

**Funcionalidades implementadas:**
-  Formulario de login/registro
-  Recuperación de contraseña
-  Reset de contraseña
-  Eliminación de cuenta
-  Búsqueda de espectáculos por artista (ej: "Natos y Waor")
-  Búsqueda de espectáculos por fecha (ej: "2026-03-14")
-  Navegación entre formularios

## Base de Datos

- **MySQL 8.0.33**
- **Base de datos:** `esiusuarios`
- **Configuración:** `application.properties` con hibernate.ddl-auto=update

## Ejemplos de Uso

### Búsqueda de Espectáculos

1. **Buscar por artista:**
   - Abre la aplicación en `http://localhost:4200`
   - En la sección "Buscar por Artista", escribe "Natos y Waor"
   - Haz clic en "Buscar" o presiona Enter
   - Verás los espectáculos de Natos y Waor (ej: 14 de marzo de 2026)

2. **Buscar por fecha:**
   - En la sección "Buscar por Fecha", selecciona "2026-03-14"
   - Haz clic en "Buscar"
   - Verás todos los espectáculos programados para esa fecha

3. **Ver disponibilidad:**
   - En los resultados de búsqueda, haz clic en "Ver Disponibilidad"
   - Se mostrará el número total de entradas y entradas libres

4. **Comprar entradas:**
   - Si hay entradas disponibles, aparecerá el botón "Comprar entrada"
   - Si no has iniciado sesión, serás redirigido al login
   - Si ya estás logueado, irás a la página de compra

### Gestión de Usuarios

- **Registro:** Crea cuenta con email y contraseña segura
- **Login:** Inicia sesión con tus credenciales
- **Recuperar contraseña:** Si olvidas tu contraseña, usa "Olvidaste tu contraseña"
- **Eliminar cuenta:** Desde el login, hay un enlace para eliminar tu cuenta

1. **Base de datos:**
   ```sql
   CREATE DATABASE esiusuarios;
   CREATE DATABASE esientradas;
   ```

2. **Actualizar tabla users (después del primer run):**
   ```sql
   \i update_users_table.sql
   ```

3. **Poblar esientradas con datos de ejemplo:**
   ```sql
   \i populate_esientradas.sql
   ```

4. **Backend - esiusuarios:**
   ```bash
   cd esiusuarios
   ./mvnw spring-boot:run
   ```

5. **Backend - esientradas:**
   ```bash
   cd esientradas
   ./mvnw spring-boot:run
   ```

6. **Frontend - esife:**
   ```bash
   cd esife
   npm install
   ng serve
   ```

## Seguridad

- **Contraseñas:** BCrypt con strength 12
- **Validación:** Mínimo 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales
- **Tokens:** UUID únicos para sesión y recuperación de contraseña
- **Expiración:** Tokens de reset expiran en 1 hora
- **Autenticación:** Bearer tokens para API
