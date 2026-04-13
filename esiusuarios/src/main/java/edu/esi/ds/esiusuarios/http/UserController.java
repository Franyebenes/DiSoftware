package edu.esi.ds.esiusuarios.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.json.JSONObject;

import edu.esi.ds.esiusuarios.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    
    @Autowired
    private UserService service;

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> credentials) {
        JSONObject jsonCredentials = new JSONObject(credentials);
        String name = jsonCredentials.optString("name");
        String password = jsonCredentials.optString("pwd");
        
        if (name.isEmpty() || password.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String result = this.service.login(name, password);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return result;
    }

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> credentials) {
        JSONObject jsonCredentials = new JSONObject(credentials);
        String email = jsonCredentials.optString("email");
        String pwd1 = jsonCredentials.optString("pwd1");
        String pwd2 = jsonCredentials.optString("pwd2");

        if (email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing fields");
        }
        if (!pwd1.equals(pwd2)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        String result = this.service.register(email, pwd1);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed");
        }
        return result;
    }
}
