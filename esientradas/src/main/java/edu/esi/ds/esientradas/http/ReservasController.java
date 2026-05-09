package edu.esi.ds.esientradas.http;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

import edu.esi.ds.esientradas.services.ReservasService;
import jakarta.servlet.http.HttpSession;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/reservas")
public class ReservasController {

    @Autowired
    private ReservasService service;
    
    @PutMapping("/reservar")
    public Long reservar(HttpSession session, @RequestParam Long idEntrada) {
        Long precioEntrada = this.service.reservar(idEntrada, session.getId());
        Long precioTotal = (Long) session.getAttribute("precioTotal");
        if (precioTotal == null) {
            precioTotal = precioEntrada;
            session.setAttribute("precioTotal", precioTotal);
        }
        else {
            precioTotal += precioEntrada;
            session.setAttribute("precioTotal", precioTotal);
        }
        return precioTotal;
     }

    @PostMapping("/reservar-multiples")
    public Long reservarMultiples(HttpSession session, @RequestBody List<Long> idEntradas) {
        Long total = this.service.reservarMultiples(idEntradas, session.getId());
        session.setAttribute("precioTotal", total);
        return total;
    }
}

