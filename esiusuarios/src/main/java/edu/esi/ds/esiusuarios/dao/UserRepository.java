package edu.esi.ds.esiusuarios.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esiusuarios.model.User;
import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Métodos que filtran usuarios activos (no eliminados)
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.email = :email")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.token = :token")
    Optional<User> findActiveByToken(@Param("token") String token);

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.resetToken = :resetToken")
    Optional<User> findActiveByResetToken(@Param("resetToken") String resetToken);

    // Métodos originales (mantener compatibilidad pero marcar como deprecated)
    @Deprecated
    Optional<User> findByEmail(String email);

    @Deprecated
    Optional<User> findByToken(String token);

    @Deprecated
    Optional<User> findByResetToken(String resetToken);

    // Soft delete method
    @Transactional // Asegura que la operación de modificación se ejecute dentro de una transacción
    @Modifying
    @Query("UPDATE User u SET u.isDeleted = true, u.updatedAt = :now WHERE u.id = :id AND u.isDeleted = false")
    int softDeleteById(@Param("id") Long id, @Param("now") LocalDateTime now);

    // Método para buscar incluyendo usuarios eliminados (para reactivación)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailIncludingDeleted(@Param("email") String email);

    // Método para obtener todos los usuarios activos
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    java.util.List<User> findAllActiveUsers();
}
