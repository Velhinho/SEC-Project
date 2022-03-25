package communication.messages;

import java.security.PublicKey;
import java.util.Objects;

public final class Account {
    private final PublicKey key;
    private final int balance;

    public Account(PublicKey key) {
        this.key = key;
        this.balance = 10;
    }

    public PublicKey key() {
        return key;
    }

    public int balance() {
        return balance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Account) obj;
        return Objects.equals(this.key, that.key) &&
                this.balance == that.balance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, balance);
    }

    @Override
    public String toString() {
        return "Account[" +
                "key=" + key + ", " +
                "balance=" + balance + ']';
    }

}
