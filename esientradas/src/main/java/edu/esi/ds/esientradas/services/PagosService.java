package edu.esi.ds.esientradas.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.model.Pago;

import jakarta.annotation.PostConstruct;

@Service
public class PagosService {

    // Se inyecta desde application.properties: stripe.secret-key=sk_test_...
    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final PagoDao pagoDao;

    public PagosService(PagoDao pagoDao) {
        this.pagoDao = pagoDao;
    }

    // Configura la clave de Stripe una sola vez al arrancar, no en cada llamada
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Crea un PaymentIntent en Stripe, persiste el pago en BD
     * y devuelve el clientSecret que necesita el frontend.
     */
    public String prepararPago(Long centimos) throws StripeException {

        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
                .setCurrency("eur")
                .setAmount(centimos)
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        Pago pago = new Pago();
        pago.setStripeIntentId(intent.getId());
        pago.setClientSecret(intent.getClientSecret());
        pago.setCreatedAt(LocalDateTime.ofInstant(Instant.ofEpochSecond(intent.getCreated()), ZoneOffset.UTC));
        pago.setAmount(intent.getAmount());
        pago.setStatus(intent.getStatus());
        pago.setPaymentMethodId(intent.getPaymentMethod());
        pago.setLivemode(intent.getLivemode());
        pagoDao.save(pago);

        return intent.getClientSecret();
    }

    /**
     * Consulta Stripe con el stripeIntentId guardado en BD,
     * actualiza el estado del pago y devuelve la entidad actualizada.
     */
    public Pago confirmarPago(String clientSecret) throws StripeException {

        Pago pago = pagoDao.findByClientSecret(clientSecret);
        if (pago == null) {
            throw new IllegalArgumentException("Pago desconocido para el clientSecret proporcionado");
        }

        // Consulta el estado real en Stripe
        PaymentIntent intent = PaymentIntent.retrieve(pago.getStripeIntentId());
        pago.setStatus(intent.getStatus());
        pago.setPaymentMethodId(intent.getPaymentMethod());
        pagoDao.save(pago);

        return pago;
    }
}