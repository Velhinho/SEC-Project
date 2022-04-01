package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class OpenAccountRequest {
    private final String key;

    public OpenAccountRequest(PublicKey publicKey){
        this.key = KeyConversion.keyToString(publicKey);
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "OpenAccountRequest{" +
                "key='" + key + '\'' +
                '}';
    }
}
