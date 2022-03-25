package communication.messages;

import java.util.Objects;

public final class CheckAccountRequest {
    private final long key;

    public CheckAccountRequest(long key) {
        this.key = key;
    }

    public long key() {
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
