package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class PendingTransfer extends Transfer {

    public PendingTransfer(String sender, String receiver, int amount, String timestamp, String signature, long wts) {
        super(sender,receiver,amount,timestamp,signature, wts);
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
        return super.toString();
    }


}
