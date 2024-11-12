package pe.edu.vallegrande.demoofb.application.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {
    private static final String ALGORITHM = "AES/OFB/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private final SecretKey key;

    public CryptoService() throws NoSuchAlgorithmException {
        key = generateKey();
    }

    private SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }

    private IvParameterSpec generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] ivBytes = new byte[IV_SIZE];
        random.nextBytes(ivBytes);
        return new IvParameterSpec(ivBytes);
    }

    public Mono<String> encrypt(String text) {
        return Mono.fromCallable(() -> {
            try {
                IvParameterSpec iv = generateIV();

                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);

                byte[] encryptedText = cipher.doFinal(text.getBytes());

                // Combine IV and encrypted text
                byte[] combined = new byte[IV_SIZE + encryptedText.length];
                System.arraycopy(iv.getIV(), 0, combined, 0, IV_SIZE);
                System.arraycopy(encryptedText, 0, combined, IV_SIZE, encryptedText.length);

                return Base64.getEncoder().encodeToString(combined);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<String> decrypt(String text) {
        return Mono.fromCallable(() -> {
            try {
                byte[] combined = Base64.getDecoder().decode(text);

                // Extract IV and encrypted text
                byte[] ivBytes = new byte[IV_SIZE];
                byte[] encryptText = new byte[combined.length - IV_SIZE];
                System.arraycopy(combined, 0, ivBytes, 0, IV_SIZE);
                System.arraycopy(combined, IV_SIZE, encryptText, 0, encryptText.length);

                IvParameterSpec iv = new IvParameterSpec(ivBytes);
                
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key, iv);

                byte[] decrypted = cipher.doFinal(encryptText);
                return new String(decrypted);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
