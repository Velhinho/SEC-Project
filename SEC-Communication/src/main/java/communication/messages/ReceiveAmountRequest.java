package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class ReceiveAmountRequest {
    private final String key;
    private final String sender;
    private final String receiver;
    private final long wts;

    public ReceiveAmountRequest(PublicKey sender, PublicKey receiver, long wts){
        this.sender = KeyConversion.keyToString(sender);
        this.receiver = KeyConversion.keyToString(receiver);
        this.key = this.receiver;
        this.wts = wts;

    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getKey() { return key; }

    public long getWts() {
        return wts;
    }
}
