package client.commands;

import client.ClientSide;
import communication.crypto.KeyConversion;

import java.util.Objects;

public final class SendCommand implements Command {
    private final String sender;
    private final String receiver;
    private final int amount;

    public SendCommand(String sender, String receiver, int amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    @Override
    public void execCommand(ClientSide clientSide) throws Exception {
        var senderKey = KeyConversion.stringToKey(sender);
        var receiverKey = KeyConversion.stringToKey(receiver);
        clientSide.sendAmountRequest(senderKey, receiverKey, amount);
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
        var that = (SendCommand) obj;
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
        return "SendCommand[" +
                "sender=" + sender + ", " +
                "receiver=" + receiver + ", " +
                "amount=" + amount + ']';
    }

}
