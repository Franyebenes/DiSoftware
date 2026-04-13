package edu.esi.ds.esientradas.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.services.PagosService;

@RestController
@RequestMapping("/pago")
@CrossOrigin(origins = "*")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @PostMapping("/prepararPago")
    public String prepararPago(@RequestBody Map<String, Object> infoPago) {
        Long centimos = ((Number) infoPago.get("centimos")).longValue();
        try {
            return pagosService.prepararPago(centimos);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Error al preparar el pago");
        } 
    }

    @PostMapping("/confirmarPago")
    public void confirmarPago(@RequestBody Map<String, Object> infoPago) {
        String clientSecret = (String) infoPago.get("clientSecret");
        try {
            pagosService.confirmarPago(clientSecret);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Error al confirmar el pago");
        }
    }
}
