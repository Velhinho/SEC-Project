package server.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class AcceptedTransfer extends Transfer {

    private final String receiverSignature;

    public AcceptedTransfer(String sender, String receiver, int amount, String timestamp, String senderSignature, String receiverSignature) {
        super(sender,receiver,amount,timestamp,senderSignature);
        this.receiverSignature = receiverSignature;
    }

    public static String DateToString(Date date){
        //SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS z");
        return formatter.format(date);
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
        return "AcceptedTransfer{" +
               "receiverSignature='" + receiverSignature + '\'' +
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