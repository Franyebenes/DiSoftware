package edu.esi.ds.esientradas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.ColaUsuarioDao;
import edu.esi.ds.esientradas.dao.EspectaculoDao;
import edu.esi.ds.esientradas.model.ColaUsuario;
import edu.esi.ds.esientradas.model.Espectaculo;

@Service
public class ColaService {

    @Autowired
    private ColaUsuarioDao colaDao;

    @Autowired
    private EspectaculoDao espectaculoDao;

    public void unirseCola(String usuarioEmail, Long espectaculoId) {
        Espectaculo espectaculo = espectaculoDao.findById(espectaculoId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espectáculo no encontrado"));

        if (!espectaculo.isTaquillaVirtual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este espectáculo no tiene taquilla virtual");
        }

        LocalDateTime now = LocalDateTime.now();
        if (espectaculo.getAperturaTaquilla() != null && now.isBefore(espectaculo.getAperturaTaquilla())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La taquilla virtual aún no está abierta");
        }

        ColaUsuario existente = colaDao.findByUsuarioEmailAndEspectaculoId(usuarioEmail, espectaculoId);
        if (existente != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya estás en la cola para este espectáculo");
        }

        ColaUsuario colaUsuario = new ColaUsuario(usuarioEmail, espectaculo);
        colaDao.save(colaUsuario);
    }

    public Integer obtenerPosicion(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(usuarioEmail, espectaculoId);
        if (colaUsuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No estás en la cola para este espectáculo");
        }

        Long usuariosDelante = colaDao.countUsuariosDelante(espectaculoId, colaUsuario.getFechaUnion());
        return usuariosDelante.intValue() + 1; // +1 porque incluye al usuario actual
    }

    public boolean esTurnoUsuario(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(usuarioEmail, espectaculoId);
        if (colaUsuario == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return colaUsuario.isTurnoActivo() &&
               colaUsuario.getTurnoExpiracion() != null &&
               now.isBefore(colaUsuario.getTurnoExpiracion());
    }

    public void asignarTurnoSiguiente(Long espectaculoId) {
        List<ColaUsuario> cola = colaDao.findByEspectaculoIdOrderByFechaUnionAsc(espectaculoId);
        for (ColaUsuario usuario : cola) {
            if (!usuario.isTurnoActivo()) {
                usuario.setTurnoActivo(true);
                usuario.setTurnoAsignado(LocalDateTime.now());
                Espectaculo espectaculo = usuario.getEspectaculo();
                usuario.setTurnoExpiracion(LocalDateTime.now().plusMinutes(espectaculo.getTiempoTurnoMinutos()));
                colaDao.save(usuario);
                break;
            }
        }
    }

    public void liberarTurno(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(usuarioEmail, espectaculoId);
        if (colaUsuario != null && colaUsuario.isTurnoActivo()) {
            colaUsuario.setTurnoActivo(false);
            colaUsuario.setTurnoAsignado(null);
            colaUsuario.setTurnoExpiracion(null);
            colaDao.save(colaUsuario);
            // Asignar turno al siguiente
            asignarTurnoSiguiente(espectaculoId);
        }
    }

    @Scheduled(fixedRateString = "30000") // cada 30 segundos
    public void procesarColas() {
        List<Espectaculo> espectaculosVirtuales = espectaculoDao.findByTaquillaVirtualTrue();
        for (Espectaculo espectaculo : espectaculosVirtuales) {
            LocalDateTime now = LocalDateTime.now();
            if (espectaculo.getAperturaTaquilla() != null && now.isAfter(espectaculo.getAperturaTaquilla())) {
                // Verificar si hay turnos expirados
                List<ColaUsuario> expirados = colaDao.findByTurnoActivoTrueAndTurnoExpiracionBefore(now);
                for (ColaUsuario expirado : expirados) {
                    liberarTurno(expirado.getUsuarioEmail(), espectaculo.getId());
                }

                // Asignar turno si no hay ninguno activo
                List<ColaUsuario> cola = colaDao.findByEspectaculoIdOrderByFechaUnionAsc(espectaculo.getId());
                boolean hayTurnoActivo = cola.stream().anyMatch(ColaUsuario::isTurnoActivo);
                if (!hayTurnoActivo && !cola.isEmpty()) {
                    asignarTurnoSiguiente(espectaculo.getId());
                }
            }
        }
    }
}