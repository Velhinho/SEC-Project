package client.commands;

import client.Register;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.KeyConversion;
import communication.messages.ReceiveAmountRequest;

import java.util.Objects;

public final class ReceiveCommand implements Command {
    private final String sender;
    private final String receiver;

    public ReceiveCommand(String sender, String receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public String execCommand(Register register) throws Exception {
        var senderKey = KeyConversion.stringToKey(sender);
        var receiverKey = KeyConversion.stringToKey(receiver);
        ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest(senderKey, receiverKey);
        var gson = new Gson();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "receiveAmount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(receiveAmountRequest)));
        return register.write(requestJson);
    }

    public String sender() {
        return sender;
    }

    public String receiver() {
        return receiver;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ReceiveCommand) obj;
        return Objects.equals(this.sender, that.sender) &&
                Objects.equals(this.receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, receiver);
    }

    @Override
    public String toString() {
        return "ReceiveCommand[" +
                "sender=" + sender + ", " +
                "receiver=" + receiver + ']';
    }

}
