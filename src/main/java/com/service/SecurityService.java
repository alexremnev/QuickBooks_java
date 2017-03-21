package com.service;

import com.intuit.ipp.services.WebhooksService;
import com.intuit.ipp.util.Config;
import com.intuit.ipp.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

@Service
public class SecurityService {
    private static final org.slf4j.Logger logger = Logger.getLogger();

    private Property property;
    private SecretKeySpec secretKey;

    @Autowired
    public SecurityService(Property property) {
        this.property = property;
    }

    @PostConstruct
    public void init() {
        try {
            secretKey = new SecretKeySpec(getEncryptionKey().getBytes("UTF-8"), "AES");
        } catch (UnsupportedEncodingException e) {
            logger.error("Error during initializing secretkeyspec ", e.getCause());
        }
    }

    public boolean isRequestValid(String signature, String payload) {
        Config.setProperty(Config.WEBHOOKS_VERIFIER_TOKEN, getVerifierKey());
        WebhooksService service = new WebhooksService();
        return service.verifyPayload(signature, payload);
    }

    private String getVerifierKey() {
        return property.getVERIFIER();
    }

    private String getEncryptionKey() {
        return property.getENCRYPTION_KEY();
    }

    public String encrypt(String plainText) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());
            return bytesToHex(byteCipherText);
        } catch (Exception e) {
            logger.error("Exception occured when application tried to encrypt entity", e.getCause());
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
            logger.error("Exception occured when applicaton tried to decrypt entity", e.getCause());
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
