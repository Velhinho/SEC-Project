package server.data;

import java.util.ArrayList;
import java.util.Objects;

public final class Account {
    private final String key;
    private int balance;
    private ArrayList<AcceptedTransfer> acceptedTransfers = new ArrayList<>();
    private ArrayList<PendingTransfer> pendingTransfers = new ArrayList<>();
    private long ts;

    public Account(String key, int balance, int ts) {
        this.key = key;
        this.balance = balance;
        this.acceptedTransfers = new ArrayList<>();
        this.pendingTransfers = new ArrayList<>();
        this.ts = ts;
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
        return "Account{" +
               "key='" + key + '\'' +
               ", balance=" + balance +
               ", transfers=" + acceptedTransfers +
               ", pendingTransfers=" + pendingTransfers +
               '}';
    }

    public void addTransfer(AcceptedTransfer acceptedTransfer) {
        acceptedTransfers.add(acceptedTransfer);}

    public void addPendingTransfer(PendingTransfer pendingTransfer){
        pendingTransfers.add(pendingTransfer);
    }

    public void changingBalance(int change){
        this.balance += change;
    }

    public ArrayList<AcceptedTransfer> getAcceptedTransfers() {
        return acceptedTransfers;
    }

    public ArrayList<PendingTransfer> getPendingTransfers() {
        return pendingTransfers;
    }

    public long getTs() {
        return ts;
    }
}
