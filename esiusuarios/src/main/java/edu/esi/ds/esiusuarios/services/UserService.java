package edu.esi.ds.esiusuarios.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esiusuarios.auxiliares.Manager;
import edu.esi.ds.esiusuarios.dao.UserRepository;
import edu.esi.ds.esiusuarios.model.User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findActiveByEmail(email);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();

        if (!user.getConfirmed()) {
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return null;
        }

        return user.getToken();
    }

    public String checkToken(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();

        if (!user.getConfirmed()) {
            return null;
        }

        return user.getEmail();
    }

    public String register(String email, String pwd1) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no es válido");
        }

        if (email.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es demasiado largo");
        }

        if (userRepository.findActiveByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado.");
        }

        if (!isPasswordSecure(pwd1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
            "La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales");
        }

        String passwordHash = passwordEncoder.encode(pwd1);
        String token = UUID.randomUUID().toString();

        User newUser = new User(email, passwordHash, token);
        userRepository.save(newUser);

        try {
            ((EmailService) Manager.getInstance().getEmailService()).sendEmail(email,
                "asunto", "Bienvenido a esiusuarios",
                "texto", buildConfirmationEmail(newUser.getToken()));
        } catch (Exception e) {
            System.err.println("Error enviando email de confirmación: " + e.getMessage());
        }

        return "Le hemos enviado la confirmación a: " + email;
    }

    public String confirmUser(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();
        user.setConfirmed(true);
        userRepository.save(user);

        return "Cuenta confirmada exitosamente";
    }

    public String forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findActiveByEmail(email);

        if (!optionalUser.isPresent()) {
            return "Si el email existe, recibirás instrucciones para resetear tu contraseña";
        }

        User user = optionalUser.get();

        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plus(1, ChronoUnit.HOURS);

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(expiry);
        userRepository.save(user);

        try {
            ((EmailService) Manager.getInstance().getEmailService()).sendEmail(email,
                "asunto", "Recuperación de contraseña - esiusuarios",
                "texto", "Para resetear tu contraseña, haz clic en el siguiente enlace: http://localhost:4200/reset-password?token=" + resetToken
            );
        } catch (Exception e) {
            System.err.println("Error enviando email de recuperación: " + e.getMessage());
        }

        return "Si el email existe, recibirás instrucciones para resetear tu contraseña";
    }

    public String resetPassword(String resetToken, String newPassword) {
        Optional<User> optionalUser = userRepository.findActiveByResetToken(resetToken);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return null;
        }

        if (!isPasswordSecure(newPassword)) {
            return null;
        }

        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Contraseña cambiada exitosamente";
    }

    public String deleteAccount(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();
    
        // Anonimizar el email para liberar el unique y mantener el historial
        user.setEmail("deleted_" + user.getId() + "@deleted.com");
        userRepository.save(user);

        int updatedRows = userRepository.softDeleteById(user.getId(), LocalDateTime.now());

        if (updatedRows > 0) {
            return "Cuenta eliminada exitosamente (soft delete)";
        } else {
            return null;
        }
    }


    public String validateTokenForEsientradas(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();

        if (!user.getConfirmed()) {
            return null;
        }

        return user.getEmail();
    }

    public String reactivateAccount(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (!optionalUser.isPresent()) {
            return null;
        }

        User user = optionalUser.get();

        if (!user.getIsDeleted()) {
            return "La cuenta ya está activa";
        }

        user.setIsDeleted(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return "Cuenta reactivada exitosamente";
    }

    public boolean isUserDeleted(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.isPresent() && optionalUser.get().getIsDeleted();
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

    //Método para construir el correo en html (tambien se puede con plantillas en Brevo)
    private String buildConfirmationEmail(String token) {
        return "<div style='font-family: Arial; padding: 20px;'>" +
           "<h2>Bienvenido a EsiEntradas 🎟️</h2>" +
           "<p>Gracias por registrarte. Confirma tu cuenta haciendo clic en el botón:</p>" +
           "<a href='http://localhost:4200/confirm?token=" + token + "' " +
           "style='background-color: #007bff; color: white; padding: 10px 20px; " +
           "text-decoration: none; border-radius: 5px;'>Confirmar cuenta</a>" +
           "<p>Si no te registraste, ignora este email.</p>" +
           "</div>";
    }
}