package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class SendAmountRequest {
    private final String key;
    private final String sender;
    private final String receiver;
    private int amount;
    private final long wts;


    public SendAmountRequest(PublicKey sender, PublicKey receiver, int amount, long wts){
        this.sender = KeyConversion.keyToString(sender);
        this.receiver = KeyConversion.keyToString(receiver);
        this.amount = amount;
        this.key = this.sender;
        this.wts = wts;
    }

    public int getAmount() {
        return amount;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public String getKey() { return key; }

    @Override
    public String toString() {
        return "SendAmountRequest{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", amount=" + amount +
                '}';
    }

    public long getWts() {
        return wts;
    }
}
