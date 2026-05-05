package edu.esi.ds.esientradas.services;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

import edu.esi.ds.esientradas.dao.ConfiguracionDao;
import edu.esi.ds.esientradas.dao.PagoDao;
import edu.esi.ds.esientradas.model.Pago;
import java.time.Instant;

@Service
public class PagosService {

    @Autowired
    private ConfiguracionDao configuracionDao;

    @Autowired
    private PagoDao pagoDao;

    public String prepararPago(Long centimos) throws StripeException {

        Stripe.apiKey = configuracionDao.findByClave("privateKey");

        PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
            .setCurrency("eur")
            .setAmount(centimos)
            .build();

        PaymentIntent intent = PaymentIntent.create(params);

        // parse the JSON so we can pick the fields we care about
        JSONObject jso = new JSONObject(intent.toJson());
        String clientSecret = jso.getString("client_secret");
        long createdAtSeconds = jso.getLong("created");
        Instant createdAt = Instant.ofEpochSecond(createdAtSeconds);

        // save a Pago including the stripe identifiers and metadata using setters
        Pago pago = new Pago();
        pago.setStripeIntentId(jso.getString("id"));
        pago.setClientSecret(jso.getString("client_secret"));
        pago.setCreatedAt(createdAt);
        pago.setAmount(jso.getLong("amount"));
        pago.setStatus(jso.getString("status"));
        pago.setPaymentMethodId(jso.optString("payment_method", null));
        pago.setLivemode(jso.getBoolean("livemode"));
        pagoDao.save(pago);

        return clientSecret;
    }

    /**
     * Comprueba con Stripe el estado de un pago ya iniciado y actualiza
     * la entidad en la base de datos. Se espera recibir el clientSecret
     * que se guardó al crear el PaymentIntent.
     */
    public Pago confirmarPago(String clientSecret) throws StripeException {
        Stripe.apiKey = configuracionDao.findByClave("privateKey");

        Pago pago = pagoDao.findByClientSecret(clientSecret);
        if (pago == null) {
            throw new IllegalArgumentException("Pago desconocido: " + clientSecret);
        }

        PaymentIntent intent = PaymentIntent.retrieve(pago.getStripeIntentId());
        String status = intent.getStatus();
        pago.setStatus(status);
        pagoDao.save(pago);

        return pago;
    }
}
