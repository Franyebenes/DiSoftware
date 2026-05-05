package edu.esi.ds.esiusuarios.http;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService service;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        JSONObject jsonCredentials = new JSONObject(credentials);
        String email = jsonCredentials.optString("email");
        String password = jsonCredentials.optString("password");
        
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

