package edu.esi.ds.esiusuarios.http;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.services.UserService;
import edu.esi.ds.esiusuarios.security.RateLimitingService;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private RateLimitingService rateLimitingService;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        JSONObject jsonCredentials = new JSONObject(credentials);
        String email = jsonCredentials.optString("email");
        String password = jsonCredentials.optString("password");

        // Rate limiting para prevenir fuerza bruta
        if (!rateLimitingService.isAllowed(email)) {
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos, o cuenta no confirmada");
        }
        return token;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> credentials) {
        JSONObject jsonCredentials = new JSONObject(credentials);
        String email = jsonCredentials.optString("email");
        String pwd1 = jsonCredentials.optString("pwd1");
        String pwd2 = jsonCredentials.optString("pwd2");

        if (email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email y contraseñas requeridas");
        }
        
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }
        
        // Validar que la contraseña cumple con los requisitos de seguridad
        if (!isPasswordSecure(pwd1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales");
        }
        
        String result = this.service.register(email, pwd1);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado o es inválido");
        }
        return result;
    }

    @GetMapping("/confirm")
    public String confirmEmail(@RequestParam String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
        }
        
        String result = this.service.confirmUser(token);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token inválido o expirado");
        }
        return result;
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> request) {
        JSONObject jsonRequest = new JSONObject(request);
        String email = jsonRequest.optString("email");
        
        if (email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requerido");
        }
        
        String result = this.service.forgotPassword(email);
        return result;
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> request) {
        JSONObject jsonRequest = new JSONObject(request);
        String resetToken = jsonRequest.optString("resetToken");
        String newPassword = jsonRequest.optString("newPassword");
        
        if (resetToken.isEmpty() || newPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token de reset y nueva contraseña requeridos");
        }
        
        // Validar que la contraseña cumple con los requisitos de seguridad
        if (!isPasswordSecure(newPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales");
        }
        
        String result = this.service.resetPassword(resetToken, newPassword);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado");
        }
        return result;
    }

    @DeleteMapping("/account")
    public String deleteAccount(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de autorización requerido");
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        String result = this.service.deleteAccount(token);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido");
        }
        return result;
    }

    @GetMapping("/validate-token")
    public String validateTokenForEsientradas(@RequestParam String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token requerido");
        }
        
        String email = this.service.validateTokenForEsientradas(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o usuario no confirmado");
        }
        
        // Retornar el email del usuario para que esientradas sepa quién está autenticado
        return email;
    }

    private boolean isPasswordSecure(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
        
        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }
}

