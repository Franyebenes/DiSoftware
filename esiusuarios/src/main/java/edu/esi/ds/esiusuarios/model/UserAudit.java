package edu.esi.ds.esiusuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registra en BD cada acción relevante de seguridad:
 * LOGIN_OK, LOGIN_FAIL, REGISTER, CONFIRM, PASSWORD_RESET,
 * PASSWORD_CHANGE, DELETE_ACCOUNT, TOO_MANY_REQUESTS
 *
 * Cumple el requisito OWASP A09 - Security Logging and Monitoring Failures
 */
@Entity
@Table(name = "users_audit")
public class UserAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    // ID del usuario afectado (puede ser null si el usuario no existe aún)
    @Column(name = "user_id")
    private Long userId;

    // Acción realizada: LOGIN_OK, LOGIN_FAIL, REGISTER, etc.
    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    // Valor anterior (p.ej. email antes de cambio)
    @Column(name = "old_value", length = 500)
    private String oldValue;

    // Valor nuevo (p.ej. email nuevo, o mensaje de error)
    @Column(name = "new_value", length = 500)
    private String newValue;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    // Para compatibilidad con la columna existente en la tabla
    @Column(name = "is_soft_delete")
    private Boolean isSoftDelete = false;

    public UserAudit() {}

    public UserAudit(Long userId, String action, String oldValue,
                     String newValue, String ipAddress) {
        this.userId      = userId;
        this.action      = action;
        this.oldValue    = oldValue;
        this.newValue    = newValue;
        this.ipAddress   = ipAddress;
        this.changedAt   = LocalDateTime.now();
        this.isSoftDelete = false;
    }

    // --- Getters ---
    public Long getAuditId()        { return auditId; }
    public Long getUserId()         { return userId; }
    public String getAction()       { return action; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getOldValue()     { return oldValue; }
    public String getNewValue()     { return newValue; }
    public String getIpAddress()    { return ipAddress; }
    public Boolean getIsSoftDelete(){ return isSoftDelete; }

    // --- Setters ---
    public void setUserId(Long userId)           { this.userId = userId; }
    public void setAction(String action)         { this.action = action; }
    public void setChangedAt(LocalDateTime t)    { this.changedAt = t; }
    public void setOldValue(String oldValue)     { this.oldValue = oldValue; }
    public void setNewValue(String newValue)     { this.newValue = newValue; }
    public void setIpAddress(String ipAddress)   { this.ipAddress = ipAddress; }
    public void setIsSoftDelete(Boolean v)       { this.isSoftDelete = v; }
}