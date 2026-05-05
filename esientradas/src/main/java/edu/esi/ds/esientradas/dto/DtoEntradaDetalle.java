package edu.esi.ds.esientradas.dto;

public record DtoEntradaDetalle(
    Long id,
    Long precioCentimos,
    String precio,
    String tipo,
    String ubicacion,
    String estado,
    Boolean disponible
) {
}
