
package edu.esi.ds.esientradas.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Espectaculo;

public interface PDFDao extends JpaRepository<Espectaculo, Long>  {

    
} 

