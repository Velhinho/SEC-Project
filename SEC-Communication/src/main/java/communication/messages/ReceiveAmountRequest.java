package communication.messages;

import communication.crypto.KeyConversion;

import java.security.PublicKey;

public class ReceiveAmountRequest {

    private String sender;
    private String receiver;

    public ReceiveAmountRequest(PublicKey sender, PublicKey receiver){
        this.sender = KeyConversion.keyToString(sender);
        this.receiver = KeyConversion.keyToString(receiver);
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }
}
