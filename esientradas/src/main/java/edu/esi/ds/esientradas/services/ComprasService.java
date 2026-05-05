package edu.esi.ds.esientradas.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.dao.CompraDao;
import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoCompra;
import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;
import edu.esi.ds.esientradas.model.Pago;

import jakarta.transaction.Transactional;

/**
 * Servicio encargado de procesar las compras definitivas. Se apoya en el
 * {@link PagosService} para comprobar con Stripe el estado real del pago y en
 * el {@link EntradaDao} para actualizar el estado de las entradas.
 */
@Service
public class ComprasService {

    @Autowired
    private PagosService pagosService;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private CompraDao compraDao;

    @Autowired
    private EmailService emailService;
    
    @Transactional
    public void realizarCompra(String usuario, DtoCompra dto) throws StripeException {
        if (dto.idEntradas() == null || dto.idEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos una entrada");
        }

        Pago pago = pagosService.confirmarPago(dto.clientSecret());
        if (!"succeeded".equalsIgnoreCase(pago.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El pago no se ha completado correctamente");
        }

        List<Entrada> entradas = StreamSupport.stream(this.entradaDao.findAllById(dto.idEntradas()).spliterator(), false)
            .toList();
        if (entradas.size() != dto.idEntradas().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alguna de las entradas solicitadas no existe");
        }

        long total = 0;
        for (Entrada entrada : entradas) {
            if (entrada.getEstado() != Estado.DISPONIBLE && entrada.getEstado() != Estado.RESERVADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada no disponible para compra: " + entrada.getId());
            }
            total += entrada.getPrecio();
            this.entradaDao.updateEstado(entrada.getId(), Estado.VENDIDA);
        }

        Compra compra = new Compra();
        compra.setUsuarioEmail(usuario);
        compra.setClientSecret(dto.clientSecret());
        compra.setCreatedAt(Instant.now());
        compra.setTotalCentimos(total);
        compra.setEntradas(entradas);
        compraDao.save(compra);

        String entradaLista = entradas.stream().map(e -> String.valueOf(e.getId())).collect(Collectors.joining(", "));
        emailService.enviar(
            usuario,
            dto.clientSecret(),
            total,
            "Compra de entradas en Esientradas",
            "Tu compra se ha confirmado.",
            "Entradas compradas: " + entradaLista,
            "Importe total: " + String.format("%.2f €", total / 100.0)
        );
    }
}

