package pe.edu.vallegrande.demoofb.application.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    private final SecretKey key;

    public CryptoService() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        key = keyGen.generateKey();
    }

    public Mono<String> encrypt(String text) {
        return Mono.fromCallable(() -> {

            // Generar nuevo IV
            SecureRandom random = new SecureRandom();
            byte[] ivBytes = new byte[IV_SIZE];
            random.nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] encrypted = cipher.doFinal(text.getBytes());
            byte[] combined = new byte[iv.getIV().length + encrypted.length];
            System.arraycopy(iv.getIV(), 0, combined, 0, iv.getIV().length);
            System.arraycopy(encrypted, 0, combined, iv.getIV().length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<String> decrypt(String text) {
        return Mono.fromCallable(() -> {
            byte[] combined = Base64.getDecoder().decode(text);

            byte[] ivBytes = new byte[IV_SIZE];
            byte[] encryptedBytes = new byte[combined.length - IV_SIZE];
            System.arraycopy(combined, 0, ivBytes, 0, IV_SIZE);
            System.arraycopy(combined, IV_SIZE, encryptedBytes, 0, encryptedBytes.length);

            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] decrypted = cipher.doFinal(encryptedBytes);
            return new String(decrypted);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
