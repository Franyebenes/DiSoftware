package edu.esi.ds.esientradas.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dao.TokenDao;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Token;
import jakarta.transaction.Transactional;

@Service
public class ReservasService {

    @Autowired private EntradaDao    dao;
    @Autowired private TokenDao      tokenDao;
    @Autowired private ColaService   colaService;

    @Transactional
    public Long reservar(Long idEntrada, String sessionId) {
        Entrada entrada = this.dao.findById(idEntrada).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        if (entrada.getEstado() != Estado.DISPONIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada no disponible");
        }

        Token token = new Token();
        token.setEntrada(entrada);
        token.setSessionId(sessionId);
        this.tokenDao.save(token);

        this.dao.updateEstado(idEntrada, Estado.RESERVADA);
        return entrada.getPrecio();
    }

    @Transactional
    public Long reservarMultiples(List<Long> idEntradas, String sessionId, String usuarioEmail) {
        if (idEntradas == null || idEntradas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No se han proporcionado entradas para reservar");
        }
        if (idEntradas.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Solo se pueden reservar hasta 5 entradas a la vez");
        }

        // Obtener el espectáculo de la primera entrada para comprobar taquilla virtual
        Entrada primeraEntrada = this.dao.findById(idEntradas.get(0)).orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Entrada no encontrada: " + idEntradas.get(0)));

        // Si el espectáculo tiene taquilla virtual, validar que el usuario tiene turno activo
        if (primeraEntrada.getEspectaculo() != null
                && primeraEntrada.getEspectaculo().isTaquillaVirtual()) {

            if (usuarioEmail == null || usuarioEmail.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para comprar entradas con taquilla virtual");
            }

            boolean tieneTurno = colaService.esTurnoUsuario(
                usuarioEmail, primeraEntrada.getEspectaculo().getId());

            if (!tieneTurno) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No tienes turno activo en la cola. Únete a la cola de espera.");
            }
        }

        // Reservar todas las entradas
        long total = 0;
        for (Long idEntrada : idEntradas) {
            Entrada entrada = this.dao.findById(idEntrada).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Entrada no encontrada: " + idEntrada));

            if (entrada.getEstado() != Estado.DISPONIBLE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Entrada no disponible: " + idEntrada);
            }

            Token token = new Token();
            token.setEntrada(entrada);
            token.setSessionId(sessionId);
            this.tokenDao.save(token);
            this.dao.updateEstado(idEntrada, Estado.RESERVADA);
            total += entrada.getPrecio();
        }

        return total;
    }

    @Scheduled(fixedRateString = "60000")
    @Transactional
    public void liberarReservasExpiradas() {
        long expiracion = System.currentTimeMillis() - (15L * 60L * 1000L);
        List<Token> expiradas = this.tokenDao.findByHoraLessThan(expiracion);
        if (expiradas.isEmpty()) return;

        for (Token token : expiradas) {
            Entrada entrada = token.getEntrada();
            if (entrada != null && entrada.getEstado() == Estado.RESERVADA) {
                this.dao.updateEstado(entrada.getId(), Estado.DISPONIBLE);
            }
        }
        this.tokenDao.deleteAll(expiradas);
    }
}