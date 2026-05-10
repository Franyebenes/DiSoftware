package edu.esi.ds.esientradas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;


@Entity
@Table(name = "pago")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @Column(name = "stripe_intent_id", unique = true, nullable = false)
    private String stripeIntentId;

    @Column(name = "client_secret", unique = true, nullable = false, length = 500)
    private String clientSecret;

    // LocalDateTime se mapea a DATETIME en MySQL sin problemas de precisión
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "amount")
    private Long amount;

    // "requires_payment_method" | "requires_confirmation" | "succeeded" | etc.
    @Column(name = "status")
    private String status;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @Column(name = "livemode")
    private Boolean livemode;

    public Pago() {}

    // --- Getters ---

    public Long getIdPago()                { return idPago; }
    public String getStripeIntentId()      { return stripeIntentId; }
    public String getClientSecret()        { return clientSecret; }
    public LocalDateTime getCreatedAt()    { return createdAt; }
    public Long getAmount()                { return amount; }
    public String getStatus()              { return status; }
    public String getPaymentMethodId()     { return paymentMethodId; }
    public Boolean getLivemode()           { return livemode; }

    // --- Setters ---

    public void setStripeIntentId(String stripeIntentId)   { this.stripeIntentId = stripeIntentId; }
    public void setClientSecret(String clientSecret)       { this.clientSecret = clientSecret; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
    public void setAmount(Long amount)                     { this.amount = amount; }
    public void setStatus(String status)                   { this.status = status; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public void setLivemode(Boolean livemode)              { this.livemode = livemode; }
}