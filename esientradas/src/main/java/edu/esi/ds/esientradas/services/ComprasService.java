package edu.esi.ds.esientradas.services;

import java.time.LocalDateTime;
import java.util.List;

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

@Service
public class ComprasService {

    @Autowired
    private PagosService pagosService;

    @Autowired
    private EntradaDao entradaDao;

    @Autowired
    private CompraDao compraDao;

    @Transactional
    public void realizarCompra(String usuario, DtoCompra dto) throws StripeException {

        // 1. Validar que vienen entradas
        if (dto.idEntradas() == null || dto.idEntradas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe seleccionar al menos una entrada");
        }

        // 2. Verificar el pago con Stripe y obtener estado actualizado
        Pago pago = pagosService.confirmarPago(dto.clientSecret());
        if (!"succeeded".equalsIgnoreCase(pago.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El pago no se ha completado. Estado actual: " + pago.getStatus());
        }

        // 3. Recuperar entradas y verificar que existen todas
        List<Entrada> entradas = (List<Entrada>) entradaDao.findAllById(dto.idEntradas());
        if (entradas.size() != dto.idEntradas().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Alguna de las entradas solicitadas no existe");
        }

        // 4. Verificar disponibilidad y calcular total
        long totalCentimos = 0;
        for (Entrada entrada : entradas) {
            if (entrada.getEstado() != Estado.DISPONIBLE && entrada.getEstado() != Estado.RESERVADA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Entrada no disponible: " + entrada.getId());
            }
            totalCentimos += entrada.getPrecio();
        }

        // 5. Marcar entradas como VENDIDAS
        for (Entrada entrada : entradas) {
            entradaDao.updateEstado(entrada.getId(), Estado.VENDIDA);
        }

        // 6. Guardar la compra en BD
        Compra compra = new Compra();
        compra.setUsuarioEmail(usuario);
        compra.setClientSecret(dto.clientSecret());
        compra.setCreatedAt(LocalDateTime.now());
        compra.setTotalCentimos(totalCentimos);
        compra.setEntradas(entradas);
        compraDao.save(compra);
    }
}