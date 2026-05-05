
package edu.esi.ds.esientradas.dto;

import java.util.List;

public record DtoCompra(
    List<Long> idEntradas,
    String clientSecret
) {}

