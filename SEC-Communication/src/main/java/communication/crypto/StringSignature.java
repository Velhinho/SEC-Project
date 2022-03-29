package communication.crypto;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class StringSignature {
    public static String sign(String plainText, PrivateKey privateKey) throws CryptoException {
        try {
            var signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            var signedBytes = signature.sign();
            return Base64.getEncoder().encodeToString(signedBytes);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage());
        }
    }

    public static boolean verify(String plainText, String signedText, PublicKey publicKey) throws CryptoException {
        try {
            var signedBytes = Base64.getDecoder().decode(signedText);
            var signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(plainText.getBytes(StandardCharsets.UTF_8));
            return signature.verify(signedBytes);
        } catch (Exception e) {
            throw new CryptoException(e.getMessage());
        }
    }
}
