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

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findActiveByEmail(email);
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!user.getConfirmed()) return null;
        if (!passwordEncoder.matches(password, user.getPasswordHash())) return null;

        return user.getToken();
    }

    public String checkToken(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!user.getConfirmed()) return null;

        return user.getEmail();
    }

    public String register(String email, String pwd1) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no es válido");

        if (email.length() > 255)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email es demasiado largo");

        if (userRepository.findActiveByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está registrado.");

        if (!isPasswordSecure(pwd1))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La contraseña debe tener al menos 8 caracteres, mayúsculas, minúsculas, números y caracteres especiales");

        String passwordHash = passwordEncoder.encode(pwd1);
        String token        = UUID.randomUUID().toString();

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
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        user.setConfirmed(true);
        userRepository.save(user);

        return "Cuenta confirmada exitosamente";
    }

    public String forgotPassword(String email) {
        Optional<User> optionalUser = userRepository.findActiveByEmail(email);
        if (optionalUser.isEmpty())
            return "Si el email existe, recibirás instrucciones para resetear tu contraseña";

        User user = optionalUser.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
        userRepository.save(user);

        try {
            ((EmailService) Manager.getInstance().getEmailService()).sendEmail(email,
                "asunto", "Recuperación de contraseña - esiusuarios",
                "texto", "Para resetear tu contraseña: http://localhost:4200/reset-password?token=" + resetToken);
        } catch (Exception e) {
            System.err.println("Error enviando email de recuperación: " + e.getMessage());
        }

        return "Si el email existe, recibirás instrucciones para resetear tu contraseña";
    }

    public String resetPassword(String resetToken, String newPassword) {
        Optional<User> optionalUser = userRepository.findActiveByResetToken(resetToken);
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now()))
            return null;

        if (!isPasswordSecure(newPassword)) return null;

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Contraseña cambiada exitosamente";
    }

    /**
     * Elimina la cuenta del usuario de forma definitiva (hard delete).
     */
    public String deleteAccount(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);
        if (optionalUser.isEmpty()) return null;

        userRepository.delete(optionalUser.get());

        return "Cuenta eliminada exitosamente";
    }

    public String validateTokenForEsientradas(String token) {
        Optional<User> optionalUser = userRepository.findActiveByToken(token);
        if (optionalUser.isEmpty()) return null;

        User user = optionalUser.get();
        if (!user.getConfirmed()) return null;

        return user.getEmail();
    }

    private boolean isPasswordSecure(String password) {
        if (password == null || password.length() < 8) return false;
        return password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*\\d.*")
            && password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?].*");
    }

    private String buildConfirmationEmail(String token) {
        return "<div style='font-family: Arial; padding: 20px;'>"
            + "<h2>Bienvenido a EsiEntradas</h2>"
            + "<p>Gracias por registrarte. Confirma tu cuenta haciendo clic en el botón:</p>"
            + "<a href='http://localhost:4200/confirm?token=" + token + "' "
            + "style='background-color:#007bff;color:white;padding:10px 20px;"
            + "text-decoration:none;border-radius:5px;'>Confirmar cuenta</a>"
            + "<p>Si no te registraste, ignora este email.</p>"
            + "</div>";
    }
}