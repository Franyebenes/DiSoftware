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

import jakarta.transaction.Transactional;

@Service
public class ColaService {

    @Autowired private ColaUsuarioDao colaDao;
    @Autowired private EspectaculoDao espectaculoDao;

    @Transactional
    public void unirseCola(String usuarioEmail, Long espectaculoId) {
        Espectaculo espectaculo = espectaculoDao.findById(espectaculoId).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espectáculo no encontrado"));

        if (!espectaculo.isTaquillaVirtual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Este espectáculo no tiene taquilla virtual");
        }

        LocalDateTime now = LocalDateTime.now();
        if (espectaculo.getAperturaTaquilla() != null
                && now.isBefore(espectaculo.getAperturaTaquilla())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "La taquilla virtual aún no está abierta. Abre el: "
                + espectaculo.getAperturaTaquilla());
        }

        ColaUsuario existente = colaDao.findByUsuarioEmailAndEspectaculoId(
            usuarioEmail, espectaculoId);
        if (existente != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Ya estás en la cola para este espectáculo");
        }

        ColaUsuario colaUsuario = new ColaUsuario(usuarioEmail, espectaculo);
        colaDao.save(colaUsuario);
    }

    @Transactional
    public Integer obtenerPosicion(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(
            usuarioEmail, espectaculoId);
        if (colaUsuario == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No estás en la cola para este espectáculo");
        }
        // Contar solo usuarios que siguen en cola (sin turno activo o con turno activo)
        // delante de este usuario por fecha de unión
        Long usuariosDelante = colaDao.countUsuariosDelante(
            espectaculoId, colaUsuario.getFechaUnion());
        return usuariosDelante.intValue() + 1;
    }

    @Transactional
    public boolean esTurnoUsuario(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(
            usuarioEmail, espectaculoId);
        if (colaUsuario == null) return false;

        LocalDateTime now = LocalDateTime.now();
        return colaUsuario.isTurnoActivo()
            && colaUsuario.getTurnoExpiracion() != null
            && now.isBefore(colaUsuario.getTurnoExpiracion());
    }

    @Transactional
    public void asignarTurnoSiguiente(Long espectaculoId) {
        Espectaculo espectaculo = espectaculoDao.findById(espectaculoId).orElse(null);
        if (espectaculo == null) return;

        int minutosTurno = espectaculo.getTiempoTurnoMinutos() != null
            ? espectaculo.getTiempoTurnoMinutos() : 5;

        List<ColaUsuario> cola = colaDao.findByEspectaculoIdOrderByFechaUnionAsc(espectaculoId);
        for (ColaUsuario usuario : cola) {
            if (!usuario.isTurnoActivo()) {
                usuario.setTurnoActivo(true);
                usuario.setTurnoAsignado(LocalDateTime.now());
                usuario.setTurnoExpiracion(LocalDateTime.now().plusMinutes(minutosTurno));
                colaDao.save(usuario);
                break;
            }
        }
    }

    @Transactional
    public void liberarTurno(String usuarioEmail, Long espectaculoId) {
        ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(
            usuarioEmail, espectaculoId);
        if (colaUsuario != null) {
            // Eliminar el registro completamente para que la posición del resto avance
            colaDao.delete(colaUsuario);
            // Asignar turno al siguiente
            asignarTurnoSiguiente(espectaculoId);
        }
    }

    @Scheduled(fixedRateString = "30000")
    @Transactional
    public void procesarColas() {
        List<Espectaculo> espectaculosVirtuales = espectaculoDao.findByTaquillaVirtualTrue();
        LocalDateTime now = LocalDateTime.now();

        for (Espectaculo espectaculo : espectaculosVirtuales) {
            if (espectaculo.getAperturaTaquilla() != null
                    && now.isBefore(espectaculo.getAperturaTaquilla())) {
                continue;
            }

            // Eliminar registros con turno expirado (en vez de solo desactivarlos)
            List<ColaUsuario> expirados = colaDao
                .findByTurnoActivoTrueAndTurnoExpiracionBefore(now);
            for (ColaUsuario expirado : expirados) {
                if (expirado.getEspectaculo() != null
                        && expirado.getEspectaculo().getId().equals(espectaculo.getId())) {
                    colaDao.delete(expirado); // eliminar en vez de desactivar
                }
            }

            // Asignar turno si no hay ninguno activo
            List<ColaUsuario> cola = colaDao
                .findByEspectaculoIdOrderByFechaUnionAsc(espectaculo.getId());
            boolean hayTurnoActivo = cola.stream().anyMatch(ColaUsuario::isTurnoActivo);
            if (!hayTurnoActivo && !cola.isEmpty()) {
                asignarTurnoSiguiente(espectaculo.getId());
            }
        }
    }
}