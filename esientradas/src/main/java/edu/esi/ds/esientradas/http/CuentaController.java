package edu.esi.ds.esientradas.http;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import edu.esi.ds.esientradas.dao.CompraDao;
import jakarta.transaction.Transactional;
import edu.esi.ds.esientradas.dto.DtoCompraHistorial;
import edu.esi.ds.esientradas.dto.DtoCompraHistorial.DtoEntradaComprada;
import edu.esi.ds.esientradas.model.Compra;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.services.UsuarioService;

@RestController
@RequestMapping("/cuenta")
@CrossOrigin(origins = "*")
public class CuentaController {

    @Autowired private CompraDao      compraDao;
    @Autowired private UsuarioService usuarioService;

    @Transactional
    @GetMapping("/mis-compras")
    public ResponseEntity<List<DtoCompraHistorial>> getMisCompras(
            @RequestParam String userToken) {

        String email = usuarioService.validateToken(userToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Compra> compras = compraDao.findByUsuarioEmailOrderByCreatedAtDesc(email);

        List<DtoCompraHistorial> resultado = compras.stream().map(compra -> {

            List<DtoEntradaComprada> entradasDto = compra.getEntradas().stream().map(entrada -> {
                Espectaculo esp   = entrada.getEspectaculo();
                String nombreEsp  = esp != null ? esp.getArtista()          : "—";
                String fechaEsp   = esp != null ? esp.getFecha().toString() : "—";
                String escenario  = esp != null && esp.getEscenario() != null
                                    ? esp.getEscenario().getNombre()        : "—";
                long precio       = entrada.getPrecio() != null ? entrada.getPrecio() : 0L;
                String precioEuros = String.format("%.2f €", precio / 100.0);

                return new DtoEntradaComprada(
                    entrada.getId(), nombreEsp, fechaEsp, escenario, precio, precioEuros
                );
            }).collect(Collectors.toList());

            return new DtoCompraHistorial(
                compra.getId(),
                compra.getCreatedAt(),
                compra.getTotalCentimos(),
                String.format("%.2f €", compra.getTotalCentimos() / 100.0),
                entradasDto
            );

        }).collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }
}