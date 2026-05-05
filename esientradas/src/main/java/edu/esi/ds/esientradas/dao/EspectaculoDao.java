package edu.esi.ds.esientradas.dao;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import edu.esi.ds.esientradas.model.Espectaculo;

public interface EspectaculoDao extends JpaRepository<Espectaculo, Long> {
    List<Espectaculo> findByArtista(String artista);

    List<Espectaculo> findByEscenarioId(Long idEscenario);

    @Query("SELECT e FROM Espectaculo e WHERE DATE(e.fecha) = :fecha")
    List<Espectaculo> findByFecha(@Param("fecha") LocalDate fecha);

    List<Espectaculo> findByTaquillaVirtualTrue();
}

