package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class SendAmountRequest {
    private String key;
    private String sender;
    private String receiver;
    private int amount;

    public SendAmountRequest(PublicKey sender, PublicKey receiver, int amount){
        this.sender = KeyConversion.keyToString(sender);
        this.receiver = KeyConversion.keyToString(receiver);
        this.amount = amount;
        this.key = this.sender;
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
        return "Account " + key + "wants to send a transfer from " + sender + " to " + receiver + ".";
    }
}
