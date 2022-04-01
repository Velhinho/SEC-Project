package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;
import java.util.Objects;

public final class CheckAccountRequest {
    private final String key;
    private final String checkKey;

    public CheckAccountRequest(PublicKey key, PublicKey checkKey) {
        this.key = KeyConversion.keyToString(key);
        this.checkKey = KeyConversion.keyToString(checkKey);
    }

    public String key() {
        return key;
    }

    public String getCheckKey() {
        return checkKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CheckAccountRequest) obj;
        return this.key == that.key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "CheckAccountRequest[" +
                "key=" + key + ']';
    }


}
