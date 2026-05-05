package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.esi.ds.esientradas.services.ColaService;

@RestController
@RequestMapping("/cola")
@CrossOrigin(origins = "*")
public class ColaController {

    @Autowired
    private ColaService colaService;

    @PostMapping("/unirse")
    public ResponseEntity<?> unirseCola(@RequestParam String usuarioEmail, @RequestParam Long espectaculoId) {
        try {
            colaService.unirseCola(usuarioEmail, espectaculoId);
            return ResponseEntity.ok("Te has unido a la cola exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/posicion")
    public ResponseEntity<?> obtenerPosicion(@RequestParam String usuarioEmail, @RequestParam Long espectaculoId) {
        try {
            Integer posicion = colaService.obtenerPosicion(usuarioEmail, espectaculoId);
            return ResponseEntity.ok(posicion);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/turno")
    public ResponseEntity<?> esTurnoUsuario(@RequestParam String usuarioEmail, @RequestParam Long espectaculoId) {
        try {
            boolean esTurno = colaService.esTurnoUsuario(usuarioEmail, espectaculoId);
            return ResponseEntity.ok(esTurno);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/liberar-turno")
    public ResponseEntity<?> liberarTurno(@RequestParam String usuarioEmail, @RequestParam Long espectaculoId) {
        try {
            colaService.liberarTurno(usuarioEmail, espectaculoId);
            return ResponseEntity.ok("Turno liberado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}