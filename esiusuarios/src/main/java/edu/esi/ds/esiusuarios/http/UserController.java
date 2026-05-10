package edu.esi.ds.esiusuarios.http;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.dao.UserRepository;
import edu.esi.ds.esiusuarios.model.User;
import edu.esi.ds.esiusuarios.security.RateLimitingService;
import edu.esi.ds.esiusuarios.security.SecurityAuditService;
import edu.esi.ds.esiusuarios.services.UserService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired private UserService          service;
    @Autowired private RateLimitingService  rateLimitingService;
    @Autowired private SecurityAuditService auditService;
    @Autowired private UserRepository       userRepository;

    // ── LOGIN ─────────────────────────────────────────────────────────────

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials,
                        HttpServletRequest request) {
        JSONObject json = new JSONObject(credentials);
        String email    = json.optString("email");
        String password = json.optString("password");
        String ip       = getIp(request);

        if (!rateLimitingService.isAllowed(email)) {
            auditService.logTooManyRequests(email, ip);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos de login. Intenta más tarde en 15 minutos.");
        }

        if (email.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Email y contraseña requeridos");
        }

        String token = service.login(email, password);
        if (token == null) {
            auditService.logLoginFail(email, ip);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Email o contraseña incorrectos, o cuenta no confirmada");
        }

        // Login correcto — obtener userId para el audit
        userRepository.findActiveByEmail(email).ifPresent(u ->
            auditService.logLoginOk(u.getId(), email, ip));

        return token;
    }

    // ── REGISTRO ──────────────────────────────────────────────────────────

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> credentials,
                           HttpServletRequest request) {
        JSONObject json = new JSONObject(credentials);
        String email    = json.optString("email");
        String pwd1     = json.optString("pwd1");
        String pwd2     = json.optString("pwd2");
        String ip       = getIp(request);

        if (email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Email y contraseñas requeridas");
        }

        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Las contraseñas no coinciden");
        }

        String result = service.register(email, pwd1);

        // Guardar audit con el userId recién creado
        userRepository.findByEmail(email).ifPresent(u ->
            auditService.logRegister(u.getId(), email, ip));

        return result;
    }

    // ── CONFIRMAR EMAIL ───────────────────────────────────────────────────

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam String token,
                               HttpServletRequest request) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
        }

        String result = service.confirmUser(token);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Token inválido o expirado");
        }

        // Buscar usuario por token para el audit
        userRepository.findByToken(token).ifPresent(u ->
            auditService.logConfirm(u.getId(), u.getEmail(), getIp(request)));

        return result;
    }

    // ── RECUPERAR CONTRASEÑA ──────────────────────────────────────────────

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> request,
                                 HttpServletRequest httpRequest) {
        JSONObject json = new JSONObject(request);
        String email    = json.optString("email");
        String ip       = getIp(httpRequest);

        if (email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }

        auditService.logForgotPassword(email, ip);
        return service.forgotPassword(email);
    }

    // ── RESET CONTRASEÑA ──────────────────────────────────────────────────

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> request,
                                HttpServletRequest httpRequest) {
        JSONObject json    = new JSONObject(request);
        String resetToken  = json.optString("resetToken");
        String newPassword = json.optString("newPassword");
        String ip          = getIp(httpRequest);

        if (resetToken.isEmpty() || newPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Token de reset y nueva contraseña requeridos");
        }

        if (!isPasswordSecure(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales");
        }

        // Buscar usuario antes de resetear para tener el userId
        userRepository.findByResetToken(resetToken).ifPresent(u ->
            auditService.logPasswordReset(u.getId(), u.getEmail(), ip));

        String result = service.resetPassword(resetToken, newPassword);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Token inválido o expirado");
        }

        return result;
    }

    // ── ELIMINAR CUENTA ───────────────────────────────────────────────────

    @DeleteMapping("/account")
    public String deleteAccount(@RequestHeader("Authorization") String authHeader,
                                HttpServletRequest request) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Token de autorización requerido");
        }

        String token = authHeader.substring(7);
        String ip    = getIp(request);

        // Guardar audit antes de eliminar (después ya no existe el usuario)
        userRepository.findByToken(token).ifPresent(u ->
            auditService.logDeleteAccount(u.getId(), u.getEmail(), ip));

        String result = service.deleteAccount(token);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }

        return result;
    }

    // ── VALIDAR TOKEN (para esientradas) ──────────────────────────────────

    @GetMapping("/validate-token")
    public String validateTokenForEsientradas(@RequestParam String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
        }

        String email = service.validateTokenForEsientradas(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Token inválido o usuario no confirmado");
        }

        return email;
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    /** Obtiene la IP real del cliente, teniendo en cuenta proxies */
    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private boolean isPasswordSecure(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*\\d.*")
            && password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
    }
}