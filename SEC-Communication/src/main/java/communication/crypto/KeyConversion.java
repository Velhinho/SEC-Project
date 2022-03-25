package communication.crypto;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


public class KeyConversion {

    public static PublicKey stringToKey(String key){
        PublicKey publicKey = null;
        try{
        byte[] publicBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        publicKey = keyFactory.generatePublic(keySpec);
        }
        catch (Exception expection){
            expection.printStackTrace();
        }
        return publicKey;
    }

    public static String keyToString(PublicKey key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

}
