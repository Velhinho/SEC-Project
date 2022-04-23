package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class AcceptedTransfer extends Transfer {

    private final String receiverSignature;

    public AcceptedTransfer(String sender, String receiver, int amount, String senderSignature, String receiverSignature, long wts, long rid) {
        super(sender,receiver,amount, senderSignature, wts, rid);
        this.receiverSignature = receiverSignature;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AcceptedTransfer that = (AcceptedTransfer) o;
        return Objects.equals(receiverSignature, that.receiverSignature);
    }

    @Override
    public String toString() {
        return "AcceptedTransfers{" +
               "sender='" + sender() + '\'' +
               ", receiver='" + receiver() +
               '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiverSignature);
    }

    public String getReceiverSignature() {
        return receiverSignature;
    }
}