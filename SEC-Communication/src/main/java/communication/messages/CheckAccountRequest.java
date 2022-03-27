package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;
import java.util.Objects;

public final class CheckAccountRequest {
    private final String key;

    public CheckAccountRequest(PublicKey key) {
        this.key = KeyConversion.keyToString(key);
    }

    public String key() {
        return key;
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
