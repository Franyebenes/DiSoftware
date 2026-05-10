package edu.esi.ds.esiusuarios.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esiusuarios.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByToken(String token);

    Optional<User> findByResetToken(String resetToken);

    // Aliases para mantener compatibilidad con UserService
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.token = :token")
    Optional<User> findActiveByToken(@Param("token") String token);

    @Query("SELECT u FROM User u WHERE u.resetToken = :resetToken")
    Optional<User> findActiveByResetToken(@Param("resetToken") String resetToken);
}