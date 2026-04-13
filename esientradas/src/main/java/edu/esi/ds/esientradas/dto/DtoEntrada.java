package edu.esi.ds.esientradas.dto;

public record DtoEntrada(
    Integer total,
    Integer libres,
    Integer reservadas,
    Integer vendidas
    ) 
{
    /*public DtoEntrada() {
    }

    public DtoEntrada(Integer total, Integer libres, Integer reservadas, Integer vendidas) {
        this.total = total;
        this.libres = libres;
        this.reservadas = reservadas;
        this.vendidas = vendidas;
    }*/
} 