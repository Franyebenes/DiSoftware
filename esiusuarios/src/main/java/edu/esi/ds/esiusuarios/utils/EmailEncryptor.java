package edu.esi.ds.esiusuarios.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Converter(autoApply = false)
public class EmailEncryptor implements AttributeConverter<String, String> {

    private static String secretKey;

    @Value("${encryption.secret-key}")
    public void setSecretKey(String key) {
        // AES necesita clave de 16 chars exactos
        EmailEncryptor.secretKey = String.format("%-16s", key).substring(0, 16);
    }

    @Override
    public String convertToDatabaseColumn(String data) {
        if (data == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Error encriptando: " + e.getMessage(), e);
        }
    }

    @Override
    public String convertToEntityAttribute(String encryptedData) {
        if (encryptedData == null) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)));
        } catch (Exception e) {
            throw new RuntimeException("Error desencriptando: " + e.getMessage(), e);
        }
    }
}