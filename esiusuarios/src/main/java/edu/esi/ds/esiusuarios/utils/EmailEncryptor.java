package edu.esi.ds.esiusuarios.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class EmailEncryptor implements AttributeConverter<String, String> {

    @Autowired
    private BasicTextEncryptor encryptor;

    @Override
    public String convertToDatabaseColumn(String data) {
        if (data == null) return null;
        try {
            return encryptor.encrypt(data);
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando datos", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            return encryptor.decrypt(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando datos", e);
        }
    }
}