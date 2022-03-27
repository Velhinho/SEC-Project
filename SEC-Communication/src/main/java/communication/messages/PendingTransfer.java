package communication.messages;

import java.util.Objects;

public final class PendingTransfer {
    private final Transfer transfer;

    public PendingTransfer(Transfer transfer) {
        this.transfer = transfer;
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

    public String sender(){
        return transfer.sender();
    }

    public String receiver(){
        return transfer.receiver();
    }

    public int amount(){
        return transfer.amount();
    }

}
