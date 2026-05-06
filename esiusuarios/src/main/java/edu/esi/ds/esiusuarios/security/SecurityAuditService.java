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