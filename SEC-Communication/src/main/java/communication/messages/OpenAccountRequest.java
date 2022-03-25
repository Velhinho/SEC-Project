package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class OpenAccountRequest {
    private final String publicKey;

    public OpenAccountRequest(PublicKey publicKey){
        this.publicKey = KeyConversion.keyToString(publicKey);
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "OpenAccountRequest{" +
                "publicKey='" + publicKey + '\'' +
                '}';
    }
}
