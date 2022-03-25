package communication.crypto;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

public class StringCipher {
    private final static String CIPHER_ALGO = "AES/ECB/PKCS5Padding";

    public static String digest(String plainText) throws Exception {
        byte[] plainBytes = plainText.getBytes();

        final String DIGEST_ALGO = "SHA-256";
        MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
        messageDigest.update(plainBytes);
        byte[] digestBytes = messageDigest.digest();

        return Base64.getEncoder().encodeToString(digestBytes);
    }

    public static String cipher(String plainText, Key key) throws Exception {
        var plainBytes = plainText.getBytes();
        var cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        var cipherBytes = cipher.doFinal(plainBytes);
        return Base64.getEncoder().encodeToString(cipherBytes);
    }

    public static String decipher(String cipherText, Key key) throws Exception {
        var cipherBytes = Base64.getDecoder().decode(cipherText);
        var cipher = Cipher.getInstance(CIPHER_ALGO);
        cipher.init(Cipher.DECRYPT_MODE, key);
        var plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes);
    }
}
