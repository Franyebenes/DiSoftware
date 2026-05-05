package edu.esi.ds.esientradas.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Token;

public interface TokenDao extends JpaRepository<Token, String> {
    List<Token> findByHoraLessThan(Long hora);
}

