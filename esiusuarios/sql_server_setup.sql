-- Script SQL Server para crear tabla users con controles de seguridad
-- Ejecutar en SQL Server Management Studio o similar

USE esiusuarios;
GO

-- Crear tabla users
CREATE TABLE users (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    email NVARCHAR(500) NOT NULL UNIQUE,  -- Encriptado
    password_hash NVARCHAR(255) NOT NULL,
    token NVARCHAR(100) NOT NULL UNIQUE,  -- Encriptado
    confirmed BIT NOT NULL DEFAULT 0,
    reset_token NVARCHAR(100) UNIQUE,     -- Encriptado
    reset_token_expiry DATETIME2,
    is_deleted BIT NOT NULL DEFAULT 0,    -- Soft delete: 0 = activo, 1 = eliminado
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Crear índices para rendimiento y seguridad
CREATE NONCLUSTERED INDEX idx_email ON users(email);
CREATE NONCLUSTERED INDEX idx_token ON users(token);
CREATE NONCLUSTERED INDEX idx_reset_token ON users(reset_token);
CREATE NONCLUSTERED INDEX idx_created_at ON users(created_at);
CREATE NONCLUSTERED INDEX idx_is_deleted ON users(is_deleted);  -- Para filtrar registros activos

-- Crear tabla de auditoría básica
CREATE TABLE users_audit (
    audit_id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT,
    action NVARCHAR(50),  -- 'INSERT', 'UPDATE', 'SOFT_DELETE', 'LOGIN', etc.
    changed_at DATETIME2 DEFAULT GETDATE(),
    old_value NVARCHAR(255),
    new_value NVARCHAR(255),
    ip_address NVARCHAR(45),
    is_soft_delete BIT DEFAULT 0  -- Para marcar auditorías de soft delete
);

-- Habilitar Transparent Data Encryption (TDE) si es posible
-- ALTER DATABASE esiusuarios SET ENCRYPTION ON;

-- Crear usuario de aplicación con permisos mínimos
-- CREATE LOGIN app_user WITH PASSWORD = 'SecurePassword123!';
-- CREATE USER app_user FOR LOGIN app_user;
-- GRANT SELECT, INSERT, UPDATE, DELETE ON users TO app_user;
-- GRANT SELECT, INSERT ON users_audit TO app_user;

-- Crear vista para usuarios activos (excluyendo soft deletes)
CREATE VIEW active_users AS
SELECT * FROM users WHERE is_deleted = 0;

-- Ejemplo de stored procedure para soft delete seguro
-- CREATE PROCEDURE SoftDeleteUser @userId BIGINT
-- AS
-- BEGIN
--     UPDATE users SET is_deleted = 1, updated_at = GETDATE() WHERE id = @userId;
--     INSERT INTO users_audit (user_id, action, is_soft_delete) VALUES (@userId, 'SOFT_DELETE', 1);
-- END;

-- NOTA SOBRE SOFT DELETE:
-- En lugar de usar DELETE, actualizar is_deleted = 1
-- Ejemplo: UPDATE users SET is_deleted = 1, updated_at = GETDATE() WHERE id = @userId
-- Las consultas deben filtrar: WHERE is_deleted = 0
-- Ejemplo: SELECT * FROM users WHERE is_deleted = 0 AND email = @email
-- Usar la vista active_users para consultas comunes: SELECT * FROM active_users

PRINT 'Base de datos configurada correctamente para esiusuarios con soft delete implementado';