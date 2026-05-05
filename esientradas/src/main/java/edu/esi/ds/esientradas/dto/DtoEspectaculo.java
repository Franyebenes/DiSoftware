package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;

public class DtoEspectaculo {

    private String artista;
    private LocalDateTime fecha;
    private String escenario;
    private Long id;
    private boolean taquillaVirtual;
    private LocalDateTime aperturaTaquilla;
    private Integer tiempoTurnoMinutos;

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public void setEscenario(String nombre) {
        this.escenario = nombre;
    }
    public String getArtista() {
        return this.artista;
    }
    public LocalDateTime getFecha() {
        return this.fecha;
    }
    public String getEscenario() {
        return this.escenario;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public boolean isTaquillaVirtual() {
        return taquillaVirtual;
    }

    public void setTaquillaVirtual(boolean taquillaVirtual) {
        this.taquillaVirtual = taquillaVirtual;
    }

    public LocalDateTime getAperturaTaquilla() {
        return aperturaTaquilla;
    }

    public void setAperturaTaquilla(LocalDateTime aperturaTaquilla) {
        this.aperturaTaquilla = aperturaTaquilla;
    }

    public Integer getTiempoTurnoMinutos() {
        return tiempoTurnoMinutos;
    }

    public void setTiempoTurnoMinutos(Integer tiempoTurnoMinutos) {
        this.tiempoTurnoMinutos = tiempoTurnoMinutos;
    }
}
