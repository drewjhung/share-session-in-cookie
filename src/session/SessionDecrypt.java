package session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SerializationUtils;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * User: AKutuzov
 * Date: 8/7/13
 * Time: 12:02 PM
 */
public class SessionDecrypt {
    private static final String KEY = "1234567890123456";
    private static Logger log = LoggerFactory.getLogger(SessionDecrypt.class);

    public String encrypt(HttpSession session) {
        try {
            Cipher aes = createChiper(Cipher.ENCRYPT_MODE);
            byte[] bytes = SerializationUtils.serialize(readAttributeMap(session));
            String encryptedSession = DatatypeConverter.printHexBinary(aes.doFinal(bytes));
            String signature = calculateSignature(bytes).toUpperCase();
            return encryptedSession + signature;
        } catch (Exception e) {
            log.error("Can't encrypt session", e);
        }
        return null;
    }

    public Map<String, Object> decrypt(String session) {
        try {
            String signature = session.substring(session.length() - 40);
            String encryptedSession = session.substring(0,session.length() - 40);

            Cipher aes = createChiper(Cipher.DECRYPT_MODE);
            byte[] bytes = aes.doFinal(DatatypeConverter.parseHexBinary(encryptedSession));

            if (!signature.equals(calculateSignature(bytes).toUpperCase())) {
                log.error("Session has been tampered with");
                return null;
            }

            //noinspection unchecked
            return (Map<String, Object>) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            log.error("Can't decrypt session", e);
        }

        return null;
    }

    private Cipher createChiper(int mode) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(mode, new SecretKeySpec(KEY.getBytes(), "AES"), new IvParameterSpec(new byte[16]));
        return aes;
    }

    private Map<String, Object> readAttributeMap(HttpSession session) {
        HashMap<String, Object> result = new HashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String el = attributeNames.nextElement();
            result.put(el, session.getAttribute(el));
        }
        return result;
    }

    private String calculateSignature(byte[] serialisedSession) {
        try {
            MessageDigest cript = MessageDigest.getInstance("SHA-1");
            cript.reset();
            cript.update(serialisedSession);
            return String.format("%1$40s", new BigInteger(1, cript.digest()).toString(16));
        } catch (Exception e) {
            log.error("Cant calculate signature", e);
        }
        return null;
    }
}
