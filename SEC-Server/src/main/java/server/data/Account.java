package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import java.util.ArrayList;
import java.util.Objects;

public final class Account {
    private final String key;
    private final int balance;
    private ArrayList<Transfer> transfers;
    private ArrayList<PendingTransfer> pendingTransfers;

    public Account(String key) {
        this.key = key;
        this.balance = 10;
    }

    public String key() {
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
