package edu.esi.ds.esientradas.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ConfiguracionDao {

    private final JdbcTemplate jdbcTemplate;

    public ConfiguracionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String findByClave(String clave) {
        String sql = "SELECT value FROM configuration WHERE type = ?";
        try {
            return jdbcTemplate.queryForObject(sql,String.class, clave);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}