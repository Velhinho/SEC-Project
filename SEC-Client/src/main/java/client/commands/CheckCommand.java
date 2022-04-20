package client.commands;

import client.ClientSide;
import client.Register;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.KeyConversion;
import communication.messages.CheckAccountRequest;

import java.util.Objects;

public final class CheckCommand implements Command {
    private final String keyString;

    public CheckCommand(String keyString) {
        this.keyString = keyString;
    }

    @Override
    public void execCommand(Register register) throws Exception {
        var key = KeyConversion.stringToKey(keyString);
        CheckAccountRequest checkAccountRequest = new CheckAccountRequest(key, key);
        var gson = new Gson();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "checkAccount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(checkAccountRequest)));
        register.read(requestJson);
    }

    public String keyString() {
        return keyString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CheckCommand) obj;
        return Objects.equals(this.keyString, that.keyString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyString);
    }

    @Override
    public String toString() {
        return "CheckCommand[" +
                "keyString=" + keyString + ']';
    }

}
