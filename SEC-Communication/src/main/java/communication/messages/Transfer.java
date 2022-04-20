package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Transfer {
    private final String signature;
    private final String  sender;
    private final String receiver;
    private final int amount;
    private final Date timestamp;
    private final long wts;

    public Transfer(String sender, String receiver, int amount, String timestamp, String signature, long wts){
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.timestamp = stringToDate(timestamp);
        this.signature = signature;
        this.wts = wts;
    }

    public static String DateToString(Date date){
        //SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss.SSS z");
        return formatter.format(date);
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

    public String sender(){
        return sender;
    }

    public String receiver(){
        return receiver;
    }

    public int amount(){
        return amount;
    }

    public String getSignature() {
        return signature;
    }

    public Date getTimestamp(){
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transfer{" +
               "signature='" + signature + '\'' +
               ", sender='" + sender + '\'' +
               ", receiver='" + receiver + '\'' +
               ", amount=" + amount +
               ", timestamp=" + timestamp +
               '}';
    }

    public long getWts() {
        return wts;
    }
}
