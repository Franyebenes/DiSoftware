package edu.esi.ds.esientradas.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.dao.CompraDao;
import edu.esi.ds.esientradas.dao.ColaUsuarioDao;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoCompra;
import edu.esi.ds.esientradas.model.ColaUsuario;
import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Pago;

import jakarta.transaction.Transactional;

@Service
public class ComprasService {

    @Autowired private PagosService             pagosService;
    @Autowired private EntradaDao               entradaDao;
    @Autowired private CompraDao                compraDao;
    @Autowired private EmailConfirmacionService emailConfirmacion;
    @Autowired private ColaUsuarioDao           colaDao;
    @Autowired private ColaService              colaService;

    @Transactional
    public void realizarCompra(String usuario, DtoCompra dto) throws StripeException {

        if (dto.idEntradas() == null || dto.idEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Debe seleccionar al menos una entrada");
        }

        // 1. Verificar pago con Stripe
        Pago pago = pagosService.confirmarPago(dto.clientSecret());
        if (!"succeeded".equalsIgnoreCase(pago.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "El pago no se ha completado. Estado: " + pago.getStatus());
        }

        // 2. Recuperar y validar entradas
        List<Entrada> entradas = (List<Entrada>) entradaDao.findAllById(dto.idEntradas());
        if (entradas.size() != dto.idEntradas().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Alguna entrada solicitada no existe");
        }

        long totalCentimos = 0;
        for (Entrada entrada : entradas) {
            if (entrada.getEstado() != Estado.DISPONIBLE
                    && entrada.getEstado() != Estado.RESERVADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Entrada no disponible: " + entrada.getId());
            }
            totalCentimos += entrada.getPrecio();
        }

        // 3. Marcar entradas como VENDIDAS
        for (Entrada entrada : entradas) {
            entradaDao.updateEstado(entrada.getId(), Estado.VENDIDA);
        }

        // 4. Guardar compra en BD
        Compra compra = new Compra();
        compra.setUsuarioEmail(usuario);
        compra.setClientSecret(dto.clientSecret());
        compra.setCreatedAt(LocalDateTime.now());
        compra.setTotalCentimos(totalCentimos);
        compra.setEntradas(entradas);
        compraDao.save(compra);

        // 5. Eliminar al usuario de la cola y asignar turno al siguiente
        //    Solo aplica si el espectáculo tiene taquilla virtual
        try {
            Entrada primeraEntrada = entradas.get(0);
            if (primeraEntrada.getEspectaculo() != null
                    && primeraEntrada.getEspectaculo().isTaquillaVirtual()) {

                Long espectaculoId = primeraEntrada.getEspectaculo().getId();
                ColaUsuario colaUsuario = colaDao.findByUsuarioEmailAndEspectaculoId(
                    usuario, espectaculoId);

                if (colaUsuario != null) {
                    colaDao.delete(colaUsuario);          // eliminar de la cola
                    colaService.asignarTurnoSiguiente(espectaculoId); // avanzar cola
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️  Error al actualizar cola tras compra: " + e.getMessage());
        }

        // 6. Enviar email de confirmación
        try {
            emailConfirmacion.enviar(usuario, compra, entradas);
        } catch (Exception e) {
            System.err.println("⚠️  Email no enviado: " + e.getMessage());
        }
    }
}