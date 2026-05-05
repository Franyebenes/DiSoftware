package edu.esi.ds.esientradas.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import java.time.LocalDate;
import java.util.List;

import edu.esi.ds.esientradas.dto.DtoEntrada;
import edu.esi.ds.esientradas.dto.DtoEntradaDetalle;
import edu.esi.ds.esientradas.dto.DtoEspectaculo;
import edu.esi.ds.esientradas.model.Escenario;
import edu.esi.ds.esientradas.model.Espectaculo;
import edu.esi.ds.esientradas.model.Entrada;
import edu.esi.ds.esientradas.services.BusquedaService;

@RestController
@RequestMapping("/busqueda")
@CrossOrigin(origins = "*")  // Acepta todas las peticiones vengan de donde vengan "", pero podríamos establecer las url que queramos
public class BusquedaController {

    @Autowired
    private BusquedaService service;

    @GetMapping("/getEntradas")
    public List<DtoEntradaDetalle> getEntradas(@RequestParam Long espectaculoId) {
        return this.service.getEntradasDisponibles(espectaculoId);
    }

    @GetMapping("/getNumeroDeEntradas")
    public Integer getNumeroDeEntradas(@RequestParam Long espectaculoId) {
        return this.service.getNumeroDeEntradas(espectaculoId); 
    }

    @GetMapping("/getNumeroDeEntradasComoDto")
    public DtoEntrada getNumeroDeEntradasComoDto(@RequestParam Long espectaculoId) {
        DtoEntrada dto = this.service.getNumeroDeEntradasComoDto(espectaculoId);
        return dto;
    }
    
    @GetMapping("/getEntradasLibres")
    public Integer getEntradasLibres(@RequestParam Long espectaculoId) {
        return this.service.getEntradasLibres(espectaculoId);
    }

    @GetMapping("/getEspectaculos")
    public List<DtoEspectaculo> getEspectaculos(@RequestParam String artista) {
        List<Espectaculo> espectaculos = this.service.getEspectaculos(artista);
        List<DtoEspectaculo> dtos = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            dto.setTaquillaVirtual(e.isTaquillaVirtual());
            dto.setAperturaTaquilla(e.getAperturaTaquilla());
            dto.setTiempoTurnoMinutos(e.getTiempoTurnoMinutos());
            return dto;
        }).toList();
        return dtos;
    }

    @GetMapping("/getEspectaculos/{idEscenario}")
    public List<DtoEspectaculo> getEspectaculos(@PathVariable Long idEscenario) {  // No es @RequestParam porque no es un parámetro de consulta, sino parte de la ruta url
        List<Espectaculo> espectaculos = this.service.getEspectaculos(idEscenario);
        List<DtoEspectaculo> dtos = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            dto.setTaquillaVirtual(e.isTaquillaVirtual());
            dto.setAperturaTaquilla(e.getAperturaTaquilla());
            dto.setTiempoTurnoMinutos(e.getTiempoTurnoMinutos());
            return dto;
        }).toList();
        return dtos;
    }  

    @GetMapping("/getEspectaculosByFecha")
    public List<DtoEspectaculo> getEspectaculosByFecha(@RequestParam String fecha) {
        LocalDate fechaParsed = LocalDate.parse(fecha); // formato esperado: YYYY-MM-DD
        List<Espectaculo> espectaculos = this.service.getEspectaculosByFecha(fechaParsed);
        List<DtoEspectaculo> dtos = espectaculos.stream().map(e -> {
            DtoEspectaculo dto = new DtoEspectaculo();
            dto.setId(e.getId());
            dto.setArtista(e.getArtista());
            dto.setFecha(e.getFecha());
            dto.setEscenario(e.getEscenario().getNombre());
            dto.setTaquillaVirtual(e.isTaquillaVirtual());
            dto.setAperturaTaquilla(e.getAperturaTaquilla());
            dto.setTiempoTurnoMinutos(e.getTiempoTurnoMinutos());
            return dto;
        }).toList();
        return dtos;
    }

    @GetMapping("/getEscenarios")
    public List<Escenario> getEscenarios() {
        return this.service.getEscenarios();
    }   

    @GetMapping("/saludar/{nombre}")
    public String saludar(@PathVariable String nombre, @RequestParam String apellido) {
        return "Hola," + nombre +" "+ apellido +", esta es la búsqueda de entradas.";
    }
}