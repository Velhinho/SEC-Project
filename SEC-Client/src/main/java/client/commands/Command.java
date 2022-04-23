package client.commands;

import client.Register;
import com.google.gson.JsonObject;

public interface Command {
    /**
     * Executes the command on the given register
     * @param register the register where the command will be executed
     * @return the response of the command
     * @throws Exception
     */
    String execCommand(Register register) throws Exception;
}
