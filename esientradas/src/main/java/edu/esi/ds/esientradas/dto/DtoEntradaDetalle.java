package edu.esi.ds.esientradas.dto;

public record DtoEntradaDetalle(
    Long id,
    Long precioCentimos,
    String precioEuros,
    String estado,
    Boolean disponible,
    String tipo,
    Integer fila,
    Integer columna,
    Integer planta,
    Integer zona
) {
}
