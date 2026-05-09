package edu.esi.ds.esientradas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compra")
    private Long idCompra;

    // Email del usuario autenticado
    @Column(name = "usuario_email", nullable = false)
    private String usuarioEmail;

    // Client secret del pago de Stripe asociado
    @Column(name = "client_secret", nullable = false, length = 500)
    private String clientSecret;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Importe total en céntimos (suma de precios de las entradas)
    @Column(name = "total_centimos", nullable = false)
    private Long totalCentimos;

    // Relación con las entradas compradas
    @ManyToMany
    @JoinTable(
        name = "compra_entrada",
        joinColumns = @JoinColumn(name = "id_compra"),
        inverseJoinColumns = @JoinColumn(name = "id_entrada")
    )
    private List<Entrada> entradas;

    public Compra() {}

    // --- Getters ---

    public Long getIdCompra()           { return idCompra; }
    public String getUsuarioEmail()     { return usuarioEmail; }
    public String getClientSecret()     { return clientSecret; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public Long getTotalCentimos()      { return totalCentimos; }
    public List<Entrada> getEntradas()  { return entradas; }

    // --- Setters ---

    public void setUsuarioEmail(String usuarioEmail)    { this.usuarioEmail = usuarioEmail; }
    public void setClientSecret(String clientSecret)    { this.clientSecret = clientSecret; }
    public void setCreatedAt(LocalDateTime createdAt)         { this.createdAt = createdAt; }
    public void setTotalCentimos(Long totalCentimos)    { this.totalCentimos = totalCentimos; }
    public void setEntradas(List<Entrada> entradas)     { this.entradas = entradas; }
}