package communication.messages;

import java.security.PublicKey;
import java.util.Objects;

public final class AuditRequest {
    private final PublicKey key;

    public AuditRequest(PublicKey key) {
        this.key = key;
    }

    public PublicKey key() {
        return key;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AuditRequest) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "AuditRequest[" +
                "key=" + key + ']';
    }

}
