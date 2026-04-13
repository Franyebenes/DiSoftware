package edu.esi.ds.esientradas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.dao.EntradaDao;
import edu.esi.ds.esientradas.dto.DtoCompra;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.model.Estado;

import jakarta.transaction.Transactional;

/**
 * Servicio encargado de procesar las compras definitivas. Se apoya en el
 * {@link PagosService} para comprobar con Stripe el estado real del pago y en
 * el {@link EntradaDao} para actualizar el estado de la entrada.
 */
@Service
public class ComprasService {

    @Autowired
    private PagosService pagosService;

    @Autowired
    private EntradaDao entradaDao;
    
    @Transactional
    public void realizarCompra(String usuario, DtoCompra dto) throws StripeException {
        // confirmar pago en stripe (actualiza también la entidad Pago)
        pagosService.confirmarPago(dto.clientSecret());

        // consultar la entrada solicitada
        Entrada entrada = this.entradaDao.findById(dto.idEntrada())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entrada no encontrada"));

        // permitimos comprar una entrada incluso si estaba sólo disponible o
        // previamente reservada; en cualquier caso pasará a vendida
        if (entrada.getEstado() != Estado.DISPONIBLE && entrada.getEstado() != Estado.RESERVADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entrada no disponible para compra");
        }

        // actualizamos el estado directamente mediante la consulta de la DAO
        this.entradaDao.updateEstado(dto.idEntrada(), Estado.VENDIDA);
    }
}

