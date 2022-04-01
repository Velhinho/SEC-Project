package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;
import java.util.Objects;

public final class AuditRequest {
    private final String key;
    private final String auditKey;

    public AuditRequest(PublicKey key, PublicKey auditKey) {
        this.key = KeyConversion.keyToString(key);
        this.auditKey = KeyConversion.keyToString(auditKey);
    }

    public String key() {
        return key;
    }

    public String getAuditKey() {
        return auditKey;
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
