package edu.esi.ds.esientradas.dao;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

public interface EntradaDao extends JpaRepository<Entrada, Long> {
    @Query(value = "UPDATE Entrada e SET e.estado = :estado WHERE e.id = :idEntrada")
    @Modifying
    void updateEstado(@Param("idEntrada") Long idEntrada, @Param("estado")Estado estado);

    List<Entrada> findByEspectaculoId(Long espectaculoId);

    Integer countByEspectaculoId(Long espectaculoId);

    Integer countByEspectaculoIdAndEstado(Long espectaculoId, Estado estado);

    @Query(value= """
            SELECT
	        count(*) AS total, 
	        sum(estado = 'DISPONIBLE') AS libres,
	        sum(estado = 'RESERVADA') AS reservadas,
	        sum(estado = 'VENDIDA') AS vendidas
        FROM entrada
        WHERE espectaculo_id = :espectaculoId
            """, nativeQuery = true)
    Object getNumeroDeEntradasComoDto(@Param("espectaculoId") Long espectaculoId);
}