package edu.esi.ds.esientradas.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import edu.esi.ds.esientradas.model.Pago;

public interface PagoDao extends JpaRepository<Pago, Long>  {
    
    // busca un pago por su client secret (generado por Stripe)
    Pago findByClientSecret(String clientSecret);
    
    // opcional: buscar por intent id si hace falta
    Pago findByStripeIntentId(String stripeIntentId);
} 

