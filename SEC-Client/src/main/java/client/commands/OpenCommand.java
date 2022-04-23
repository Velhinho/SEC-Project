package client.commands;

import client.Register;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.KeyConversion;
import communication.messages.OpenAccountRequest;

import java.util.Objects;

public final class OpenCommand implements Command {
    private final String keyString;

    public OpenCommand(String keyString) {
        System.out.println(keyString);
        this.keyString = keyString;
    }

    @Override
    public String execCommand(Register register) throws Exception {
        var key = KeyConversion.stringToKey(keyString);
        OpenAccountRequest openAccountRequest = new OpenAccountRequest(key);
        var gson = new Gson();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "openAccount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(openAccountRequest)));
        return register.write(requestJson);
    }



    public String keyString() {
        return keyString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OpenCommand) obj;
        return Objects.equals(this.keyString, that.keyString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyString);
    }

    @Override
    public String toString() {
        return "OpenCommand[" +
                "keyString=" + keyString + ']';
    }

}
