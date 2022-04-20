package server.data;

import java.util.Date;
import java.util.Objects;

public final class PendingTransferOld {
    private final AcceptedTransfer acceptedTransfer;
    private final String signature;

    public PendingTransferOld(AcceptedTransfer acceptedTransfer, String signature) {
        this.acceptedTransfer = acceptedTransfer;
        this.signature = signature;
    }

    public AcceptedTransfer transfer() {
        return acceptedTransfer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PendingTransferOld) obj;
        return Objects.equals(this.acceptedTransfer, that.acceptedTransfer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(acceptedTransfer);
    }

    @Override
    public String toString() {
        return "PendingTransfer[" +
               "transfer=" + acceptedTransfer + ']';
    }

    public String sender(){
        return acceptedTransfer.sender();
    }

    public String receiver(){
        return acceptedTransfer.receiver();
    }

    public int amount(){
        return acceptedTransfer.amount();
    }

    public String getSignature() {
        return signature;
    }

    public Date getTimestamp(){
        return acceptedTransfer.getTimestamp();
    }
}
