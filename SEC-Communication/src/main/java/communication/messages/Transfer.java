package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public final class Transfer {
    private final String  sender;
    private final String receiver;
    private final int amount;
    private final Date timestamp;
    private final long wts;

<<<<<<< Updated upstream:SEC-Server/src/main/java/server/data/Transfer.java
    public Transfer(String sender, String receiver, int amount, String timestamp) {
=======
    public Transfer(String sender, String receiver, int amount, String timestamp, String signature, long wts){
>>>>>>> Stashed changes:SEC-Communication/src/main/java/communication/messages/Transfer.java
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = stringToDate(timestamp);
<<<<<<< Updated upstream:SEC-Server/src/main/java/server/data/Transfer.java
=======
        this.signature = signature;
        this.wts = wts;
    }

    public static String DateToString(Date date){
        //SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS z");
        return formatter.format(date);
>>>>>>> Stashed changes:SEC-Communication/src/main/java/communication/messages/Transfer.java
    }

    public static Date stringToDate(String timestamp){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS z");
        Date date = null;
        try{
            date = formatter.parse(timestamp);
        }catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return date;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public static String DateToString(Date date){
        //SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS z");
        return formatter.format(date);
    }

    public String sender() {
        return sender;
    }

    public String receiver() {
        return receiver;
    }

    public int amount() {
        return amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Transfer) obj;
        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.receiver, that.receiver) &&
                this.amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, receiver, amount);
    }

    @Override
    public String toString() {
        return "Transfer[" +
                "sender=" + sender + ", " +
                "receiver=" + receiver + ", " +
                "amount=" + amount + ']';
    }

<<<<<<< Updated upstream:SEC-Server/src/main/java/server/data/Transfer.java
}
=======
    public long getWts() {
        return wts;
    }
}
>>>>>>> Stashed changes:SEC-Communication/src/main/java/communication/messages/Transfer.java
