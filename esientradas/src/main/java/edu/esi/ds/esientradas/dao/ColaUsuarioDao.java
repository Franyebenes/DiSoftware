package edu.esi.ds.esientradas.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.ColaUsuario;

public interface ColaUsuarioDao extends JpaRepository<ColaUsuario, Long> {
    List<ColaUsuario> findByEspectaculoIdOrderByFechaUnionAsc(Long espectaculoId);
    ColaUsuario findByUsuarioEmailAndEspectaculoId(String usuarioEmail, Long espectaculoId);
    List<ColaUsuario> findByTurnoActivoTrueAndTurnoExpiracionBefore(LocalDateTime now);
    @Query("SELECT COUNT(c) FROM ColaUsuario c WHERE c.espectaculo.id = :espectaculoId AND c.fechaUnion < :fechaUnion")
    Long countUsuariosDelante(@Param("espectaculoId") Long espectaculoId, @Param("fechaUnion") LocalDateTime fechaUnion);
}