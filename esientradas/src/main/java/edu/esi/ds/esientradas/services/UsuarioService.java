package edu.esi.ds.esientradas.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    public String validateToken(String userToken) {
        String endpoint = "http://localhost:8081/users/validate-token";
        RestTemplate rest = new RestTemplate();
        try{
            String userEmail = rest.getForObject(endpoint + "?token=" + userToken, String.class);
            if(userEmail == null || userEmail.isEmpty()){
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token Inválido");
            }
            return userEmail;
        } catch (RestClientException ex){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No se puede validar el token", ex);
        }
    }
}

