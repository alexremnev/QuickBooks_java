package com.service;

import com.intuit.ipp.services.WebhooksService;
import com.intuit.ipp.util.Config;
import com.intuit.ipp.util.Logger;
import com.util.Property;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

@Service
public class SecurityService {
    private static final org.slf4j.Logger logger = Logger.getLogger();

    private SecretKeySpec secretKey;


    @PostConstruct
    public void init() {
        try {
            secretKey = new SecretKeySpec(Property.ENCRYPTION_KEY.getBytes("UTF-8"), "AES");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error during initializing secretkeyspec ", e);
        }
    }

    public boolean isRequestValid(String signature, String payload) {
        Config.setProperty(Config.WEBHOOKS_VERIFIER_TOKEN, Property.VERIFIER);
        WebhooksService service = new WebhooksService();
        return service.verifyPayload(signature, payload);
    }

       public String encrypt(String plainText) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
            return bytesToHex(byteCipherText);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to encrypt entity", e);
            return null;
        }
    }

    public String decrypt(String byteCipherText) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] bytePlainText = aesCipher.doFinal(hexToBytes(byteCipherText));
            return new String(bytePlainText);
        } catch (Exception e) {
            logger.error("Exception occured when applicaton tried to decrypt entity", e);
            return null;
        }
    }

    private String bytesToHex(byte[] hash) {
        return DatatypeConverter.printHexBinary(hash);
    }

    private byte[] hexToBytes(String hash) {
        return DatatypeConverter.parseHexBinary(hash);
    }
}
