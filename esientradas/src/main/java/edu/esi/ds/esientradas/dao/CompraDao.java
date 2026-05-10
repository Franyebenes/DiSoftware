package edu.esi.ds.esientradas.dao;
 
import java.util.List;
 
import org.springframework.data.jpa.repository.JpaRepository;
 
import edu.esi.ds.esientradas.model.Compra;
 
public interface CompraDao extends JpaRepository<Compra, Long> {
 
    List<Compra> findByUsuarioEmailOrderByCreatedAtDesc(String usuarioEmail);
}
 