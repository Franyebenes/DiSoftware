package edu.esi.ds.esientradas.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.dao.EscenarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaDetalle;
import edu.esi.ds.esientradas.dao.EntradaDao;

@Service
public class BusquedaService {

    @Autowired
    private EscenarioDao dao;

    @Autowired
    private EspectaculoDao espectuloDao;

    @Autowired
    private EntradaDao entradaDao;

    public List<Escenario> getEscenarios() {
        return this.dao.findAll();
    }

    public List<Espectaculo> getEspectaculos(String artista) {
        return this.espectuloDao.findByArtista(artista);
    }

    public List<Espectaculo> getEspectaculos(Long idEscenario) {
        return this.espectuloDao.findByEscenarioId(idEscenario);
    }

    public List<Espectaculo> getEspectaculosByFecha(LocalDate fecha) {
        return this.espectuloDao.findByFecha(fecha);
    }

    public List<Entrada> getEntradas(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    public List<DtoEntradaDetalle> getEntradasDisponibles(Long espectaculoId) {
        return this.entradaDao.findByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE).stream()
            .map(entrada -> {
                String tipo = entrada instanceof edu.esi.ds.esientradas.model.Precisa ? "Ubicación precisa" : "Zona";
                String ubicacion;
                if (entrada instanceof edu.esi.ds.esientradas.model.Precisa precisa) {
                    ubicacion = String.format("Fila %d, Columna %d, Planta %d", precisa.getFila(), precisa.getColumna(), precisa.getPlanta());
                } else if (entrada instanceof edu.esi.ds.esientradas.model.DeZona deZona) {
                    ubicacion = "Zona " + deZona.getZona();
                } else {
                    ubicacion = "Ubicación genérica";
                }
                String precio = String.format("%.2f €", entrada.getPrecio() / 100.0);
                return new DtoEntradaDetalle(
                    entrada.getId(),
                    entrada.getPrecio(),
                    precio,
                    tipo,
                    ubicacion,
                    entrada.getEstado().name(),
                    entrada.getEstado() == Estado.DISPONIBLE
                );
            }).toList();
    }

    public Integer getNumeroDeEntradas(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoId(espectaculoId);
    }

    public Integer getEntradasLibres(Long espectaculoId) {
        return this.entradaDao.countByEspectaculoIdAndEstado(espectaculoId, Estado.DISPONIBLE);
    }

    public DtoEntrada getNumeroDeEntradasComoDto(Long espectaculoId) {
        Object o = this.entradaDao.getNumeroDeEntradasComoDto(espectaculoId);
        DtoEntrada dto = new DtoEntrada(
            ((Number)((Object[])o)[0]).intValue(),
            ((Number)((Object[])o)[1]).intValue(),
            ((Number)((Object[])o)[2]).intValue(),
            ((Number)((Object[])o)[3]).intValue()
        );
        return dto;
    }

}