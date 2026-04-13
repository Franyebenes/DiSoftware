package edu.esi.ds.esientradas.http;

import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.StripeException;

import edu.esi.ds.esientradas.services.UsuarioService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import edu.esi.ds.esientradas.services.ComprasService;
import edu.esi.ds.esientradas.dto.DtoCompra;

@RestController
@RequestMapping("/compras")
@CrossOrigin(origins="*")
public class ComprasController {

    @Autowired 
    private UsuarioService usuarioService;

    @Autowired 
    private ComprasService comprasService;  

    @PostMapping("/comprar")
    public ResponseEntity<?> comprar(@RequestBody DtoCompra dto,
                                     @RequestParam String userToken) throws StripeException {
        // validar token
        String user = usuarioService.checkToken(userToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // delegar a servicio de compras (verifica con Stripe, guarda en BD…)
        comprasService.realizarCompra(user, dto);
        return ResponseEntity.ok("Compra registrada");
    }
}
