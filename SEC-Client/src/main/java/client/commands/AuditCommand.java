package client.commands;

import client.ClientSide;
import client.Register;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.KeyConversion;
import communication.messages.AuditRequest;
import communication.messages.CheckAccountRequest;

import java.util.Objects;

public final class AuditCommand implements Command {
    private final String keyString;

    public AuditCommand(String keyString) {
        this.keyString = keyString;
    }

    @Override
    public void execCommand(Register register) throws Exception {
        var key = KeyConversion.stringToKey(keyString);
        AuditRequest auditRequest = new AuditRequest(key, key);
        var gson = new Gson();
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "audit");
        requestJson.add("request", JsonParser.parseString(gson.toJson(auditRequest)));
        register.read(requestJson);
    }

    public String keyString() {
        return keyString;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AuditCommand) obj;
        return Objects.equals(this.keyString, that.keyString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyString);
    }

    @Override
    public String toString() {
        return "AuditCommand[" +
                "keyString=" + keyString + ']';
    }

}
