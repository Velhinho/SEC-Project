package client.commands;

import client.ClientSide;
import communication.crypto.KeyConversion;

import java.util.Objects;

public final class OpenCommand implements Command {
    private final String keyString;

    public OpenCommand(String keyString) {
        System.out.println(keyString);
        this.keyString = keyString;
    }

    @Override
    public void execCommand(ClientSide clientSide) throws Exception {
        var key = KeyConversion.stringToKey(keyString);
        clientSide.openAccount(key);
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
