package communication.messages;

import java.util.Objects;

public final class PendingTransfer {
    private final Transfer transfer;

<<<<<<< Updated upstream:SEC-Server/src/main/java/server/data/PendingTransfer.java
    public PendingTransfer(Transfer transfer) {
        this.transfer = transfer;
=======
    public PendingTransfer(String sender, String receiver, int amount, String timestamp, String signature, long wts) {
        super(sender,receiver,amount,timestamp,signature, wts);
>>>>>>> Stashed changes:SEC-Communication/src/main/java/communication/messages/PendingTransfer.java
    }

    public Transfer transfer() {
        return transfer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PendingTransfer) obj;
        return Objects.equals(this.transfer, that.transfer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transfer);
    }

    @Override
    public String toString() {
        return "PendingTransfer[" +
                "transfer=" + transfer + ']';
    }

<<<<<<< Updated upstream:SEC-Server/src/main/java/server/data/PendingTransfer.java
    public String sender(){
        return transfer.sender();
    }

    public String receiver(){
        return transfer.receiver();
    }

    public int amount(){
        return transfer.amount();
    }

=======
>>>>>>> Stashed changes:SEC-Communication/src/main/java/communication/messages/PendingTransfer.java
}
