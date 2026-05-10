package edu.esi.ds.esientradas.http;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.services.ReservasService;
import edu.esi.ds.esientradas.services.UsuarioService;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired private ReservasService service;
    @Autowired private UsuarioService  usuarioService;

    @PutMapping("/reservar")
    public Long reservar(HttpSession session, @RequestParam Long idEntrada) {
        Long precioEntrada = this.service.reservar(idEntrada, session.getId());
        Long precioTotal = (Long) session.getAttribute("precioTotal");
        precioTotal = precioTotal == null ? precioEntrada : precioTotal + precioEntrada;
        session.setAttribute("precioTotal", precioTotal);
        return precioTotal;
    }

    @PostMapping("/reservar-multiples")
    public Long reservarMultiples(
            HttpSession session,
            @RequestBody List<Long> idEntradas,
            // userToken es opcional — solo se usa si el espectáculo tiene taquilla virtual
            @RequestParam(required = false) String userToken) {

        // Resolver email del usuario si viene token
        String usuarioEmail = null;
        if (userToken != null && !userToken.isBlank()) {
            usuarioEmail = usuarioService.validateToken(userToken);
        }

        Long total = this.service.reservarMultiples(idEntradas, session.getId(), usuarioEmail);
        session.setAttribute("precioTotal", total);
        return total;
    }
}