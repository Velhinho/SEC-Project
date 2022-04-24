package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class PendingTransfer extends Transfer {
    private final String senderSignature;

    public PendingTransfer(String sender, String receiver, int amount, String signature, long wts, long rid) {
        super(sender,receiver,amount, wts, rid);
        this.senderSignature = signature;
    }

    public String getSenderSignature() {
        return senderSignature;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "PendingTransfer{" +
        "sender='" + sender() + '\'' +
        ", receiver='" + receiver() + '\'' +
        ", amount=" + amount() +
        '}';
    }


}
