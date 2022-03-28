package client.commands;

import client.ClientSide;

public interface Command {
    void execCommand(ClientSide clientSide) throws Exception;
}
