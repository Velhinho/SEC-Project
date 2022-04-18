package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;
import java.util.Objects;

public final class AuditRequest {
    private final String key;
    private final String auditKey;
    private final long rid;

    public AuditRequest(PublicKey key, PublicKey auditKey, long rid) {
        this.key = KeyConversion.keyToString(key);
        this.auditKey = KeyConversion.keyToString(auditKey);
        this.rid = rid;
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

    public long getRid() {
        return rid;
    }
}
