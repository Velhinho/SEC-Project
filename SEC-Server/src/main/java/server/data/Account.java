package server.data;

import communication.messages.PendingTransfer;
import communication.messages.Transfer;

import java.util.ArrayList;
import java.util.Objects;

public final class Account {
    private final String key;
    private int balance;
    private ArrayList<Transfer> transfers = new ArrayList<>();
    private ArrayList<PendingTransfer> pendingTransfers = new ArrayList<>();

    public Account(String key, int balance) {
        this.key = key;
        this.balance = balance;
        this.transfers = new ArrayList<>();
        this.pendingTransfers = new ArrayList<>();
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
                ", transfers=" + transfers +
                ", pendingTransfers=" + pendingTransfers +
                '}';
    }

    public void addTransfer(Transfer transfer) {transfers.add(transfer);}

    public void addPendingTransfer(Transfer transfer){
        pendingTransfers.add(new PendingTransfer(transfer));
    }

    public void changingBalance(int change){
        this.balance += change;
    }

    public ArrayList<Transfer> getTransfers() {
        return transfers;
    }

    public ArrayList<PendingTransfer> getPendingTransfers() {
        return pendingTransfers;
    }

    public void acceptPendingTransferAsSender(String receiver){
        PendingTransfer senderAcceptedTransfer = null;
        for (communication.messages.PendingTransfer senderPendingTransfer : pendingTransfers) {
            if (senderPendingTransfer.sender().equals(key) && senderPendingTransfer.receiver().equals(receiver)) {
                senderAcceptedTransfer = senderPendingTransfer;
                break;
            }
        }
        if (senderAcceptedTransfer != null){
            changingBalance(-senderAcceptedTransfer.amount());
            transfers.add(senderAcceptedTransfer.transfer());
            pendingTransfers.remove(senderAcceptedTransfer);
        }
    }

    public void acceptPendingTransferAsReceiver(String sender){
        PendingTransfer receiverAcceptedTransfer = null;
        for (communication.messages.PendingTransfer receiverPendingTransfer : pendingTransfers) {
            if (receiverPendingTransfer.sender().equals(sender) && receiverPendingTransfer.receiver().equals(key)) {
                receiverAcceptedTransfer = receiverPendingTransfer;
                break;
            }
        }
        if (receiverAcceptedTransfer != null){
            changingBalance(receiverAcceptedTransfer.amount());
            transfers.add(receiverAcceptedTransfer.transfer());
            pendingTransfers.remove(receiverAcceptedTransfer);
        }
    }
}
