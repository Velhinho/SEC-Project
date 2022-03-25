package communication.crypto;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

class AESKeyGenerator {
    public static Key generateKey() throws NoSuchAlgorithmException {
        var keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(128);
        return keyGen.generateKey();
    }
}