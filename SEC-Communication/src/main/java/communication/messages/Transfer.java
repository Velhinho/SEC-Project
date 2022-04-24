package communication.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Transfer implements Comparable<Transfer> {
    private final String  sender;
    private final String receiver;
    private final int amount;
    private final long wts;
    private final long rid;

    public Transfer(String sender, String receiver, int amount, long wts, long rid){
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.wts = wts;
        this.rid = rid;
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


    @Override
    public String toString() {
        return "Transfer{" +
               "sender='" + sender + '\'' +
               ", receiver='" + receiver + '\'' +
               ", amount=" + amount +
               '}';
    }

    public long getWts() {
        return wts;
    }

    @Override
    public int compareTo(Transfer t) {
        return Long.compare(getWts(), t.getWts());
    }

    public long getRid() {
        return rid;
    }
}
