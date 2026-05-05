package edu.esi.ds.esientradas.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ColaUsuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String usuarioEmail;
    private LocalDateTime fechaUnion;
    private LocalDateTime turnoAsignado;
    private LocalDateTime turnoExpiracion;
    private boolean turnoActivo = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "espectaculo_id", nullable = false)
    private Espectaculo espectaculo;

    public ColaUsuario() {}

    public ColaUsuario(String usuarioEmail, Espectaculo espectaculo) {
        this.usuarioEmail = usuarioEmail;
        this.espectaculo = espectaculo;
        this.fechaUnion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuarioEmail() {
        return usuarioEmail;
    }

    public void setUsuarioEmail(String usuarioEmail) {
        this.usuarioEmail = usuarioEmail;
    }

    public LocalDateTime getFechaUnion() {
        return fechaUnion;
    }

    public void setFechaUnion(LocalDateTime fechaUnion) {
        this.fechaUnion = fechaUnion;
    }

    public LocalDateTime getTurnoAsignado() {
        return turnoAsignado;
    }

    public void setTurnoAsignado(LocalDateTime turnoAsignado) {
        this.turnoAsignado = turnoAsignado;
    }

    public LocalDateTime getTurnoExpiracion() {
        return turnoExpiracion;
    }

    public void setTurnoExpiracion(LocalDateTime turnoExpiracion) {
        this.turnoExpiracion = turnoExpiracion;
    }

    public boolean isTurnoActivo() {
        return turnoActivo;
    }

    public void setTurnoActivo(boolean turnoActivo) {
        this.turnoActivo = turnoActivo;
    }

    public Espectaculo getEspectaculo() {
        return espectaculo;
    }

    public void setEspectaculo(Espectaculo espectaculo) {
        this.espectaculo = espectaculo;
    }
}