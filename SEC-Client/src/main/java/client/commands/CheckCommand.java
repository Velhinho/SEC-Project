package client.commands;

import client.ClientSide;
import communication.crypto.KeyConversion;

import java.util.Objects;

public final class CheckCommand implements Command {
    private final String keyString;

    public CheckCommand(String keyString) {
        this.keyString = keyString;
    }

    @Override
    public void execCommand(ClientSide clientSide) throws Exception {
        var key = KeyConversion.stringToKey(keyString);
        clientSide.checkAccount(key);
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
