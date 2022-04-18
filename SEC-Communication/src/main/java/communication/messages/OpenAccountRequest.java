package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class OpenAccountRequest {
    private final String key;
    private final long wts;

    public OpenAccountRequest(PublicKey publicKey, long wts){
        this.key = KeyConversion.keyToString(publicKey);
        this.wts = wts;
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

    public long getWts() {
        return wts;
    }
}
