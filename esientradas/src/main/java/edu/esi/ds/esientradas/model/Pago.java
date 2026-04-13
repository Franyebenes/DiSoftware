package edu.esi.ds.esientradas.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.Instant;

@Entity
public class Pago {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    public Long idPago;

    @Column(name = "stripe_intent_id")
    public String stripeIntentId;

    @Column(name = "client_secret")
    public String clientSecret;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "amount")
    public Long amount;

    @Column(name = "status")
    public String status;

    @Column(name = "payment_method_id")
    public String paymentMethodId;

    @Column(name = "livemode")
    public Boolean livemode;

    // JPA requires a no-argument constructor
    public Pago() {
    }

    public Pago(Long idPago) {
        this.idPago = idPago;
    }

    // full constructor with all stripe metadata
    public Pago(Long idPago, String stripeIntentId, String clientSecret,
                Instant createdAt, Long amount, String status,
                String paymentMethodId, Boolean livemode) {
        this.idPago = idPago;
        this.stripeIntentId = stripeIntentId;
        this.clientSecret = clientSecret;
        this.createdAt = createdAt;
        this.amount = amount;
        this.status = status;
        this.paymentMethodId = paymentMethodId;
        this.livemode = livemode;
    }

    public Long getId() {
        return idPago;
    }

    public String getStripeIntentId() {
        return stripeIntentId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public Boolean getLivemode() {
        return livemode;
    }

    public void setId(Long idPago) {
        this.idPago = idPago;
    }

    public void setStripeIntentId(String stripeIntentId) {
        this.stripeIntentId = stripeIntentId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public void setLivemode(Boolean livemode) {
        this.livemode = livemode;
    }
}
