package client.commands;

import client.Register;

public interface Command {
    void execCommand(Register register) throws Exception;
}
