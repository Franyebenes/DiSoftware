package edu.esi.ds.esientradas.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DtoCompraHistorial(
    Long id,
    LocalDateTime fecha,
    Long totalCentimos,
    String totalEuros,
    List<DtoEntradaComprada> entradas
) {
    public record DtoEntradaComprada(
        Long idEntrada,
        String espectaculo,
        String fechaEspectaculo,
        String escenario,
        Long precioCentimos,
        String precioEuros
    ) {}
}