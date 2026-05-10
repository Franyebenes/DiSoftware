package edu.esi.ds.esiusuarios.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.esi.ds.esiusuarios.model.UserAudit;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, Long> {
}