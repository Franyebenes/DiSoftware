package edu.esi.ds.esiusuarios.security;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esiusuarios.dao.UserAuditRepository;
import edu.esi.ds.esiusuarios.model.UserAudit;

/**
 * Registra eventos de seguridad tanto en fichero de log como en BD (users_audit).
 * Cumple OWASP A09 - Security Logging and Monitoring Failures.
 */
@Service
public class SecurityAuditService {

    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private UserAuditRepository auditRepository;

    // ── Métodos públicos llamados desde UserController ────────────────────

    /** Login correcto */
    public void logLoginOk(Long userId, String email, String ip) {
        log("LOGIN_OK", userId, email, null, ip);
    }

    /** Login fallido (email o contraseña incorrectos) */
    public void logLoginFail(String email, String ip) {
        log("LOGIN_FAIL", null, email, "Credenciales incorrectas", ip);
    }

    /** Demasiados intentos de login */
    public void logTooManyRequests(String email, String ip) {
        log("TOO_MANY_REQUESTS", null, email, "Rate limit alcanzado", ip);
    }

    /** Registro de nuevo usuario */
    public void logRegister(Long userId, String email, String ip) {
        log("REGISTER", userId, null, email, ip);
    }

    /** Confirmación de cuenta por email */
    public void logConfirm(Long userId, String email, String ip) {
        log("CONFIRM", userId, email, "Cuenta confirmada", ip);
    }

    /** Solicitud de recuperación de contraseña */
    public void logForgotPassword(String email, String ip) {
        log("FORGOT_PASSWORD", null, email, "Email de recuperación solicitado", ip);
    }

    /** Cambio de contraseña (reset) */
    public void logPasswordReset(Long userId, String email, String ip) {
        log("PASSWORD_RESET", userId, email, "Contraseña cambiada", ip);
    }

    /** Eliminación de cuenta */
    public void logDeleteAccount(Long userId, String email, String ip) {
        log("DELETE_ACCOUNT", userId, email, "Cuenta eliminada", ip);
    }

    // ── Método interno ────────────────────────────────────────────────────

    private void log(String action, Long userId, String oldValue,
                     String newValue, String ip) {
        // 1. Log en fichero
        auditLogger.info("AUDIT - action={}, userId={}, oldValue={}, newValue={}, ip={}, timestamp={}",
            action, userId, oldValue, newValue, ip, LocalDateTime.now());

        // 2. Guardar en BD (users_audit) — nunca fallar la operación principal
        try {
            UserAudit audit = new UserAudit(userId, action, oldValue, newValue, ip);
            auditRepository.save(audit);
        } catch (Exception e) {
            auditLogger.error("Error guardando audit en BD: {}", e.getMessage());
        }
    }
}