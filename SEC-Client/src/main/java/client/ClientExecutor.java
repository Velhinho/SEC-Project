package client;

import client.commands.Command;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.BroadcastChannel;
import communication.channel.ClientChannel;
import communication.crypto.KeyConversion;
import communication.messages.TimeStampRequest;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class ClientExecutor {

    private final KeyPair keyPair;
    private final int quorumSize;
    private final int numberOfReplicas;

    /**
     * Creates a ClientExecutor. A ClientExecutor is a version of the Client that is designed to be able to be
     * used in JUNIT tests.
     * @param keyPair A pair of Keys. Contains both a public and private key
     * @param f The number of faults that must be tolerated.
     */


    public ClientExecutor(KeyPair keyPair, int f){
        this.keyPair = keyPair;
        this.numberOfReplicas = 3*f + 1;
        this.quorumSize = 2*f + 1;
    }

    /**
     * Gets the WTS necessary for the client to be able to do operations and returns a Register
     * with the updated wts
     * @return a register with an updated wts
     */

    public Register getWts(String key){
        Register register = null;
        try {

            ArrayList<ClientChannel> clientChannels = new ArrayList<>();

            for (int i = 1; i <= numberOfReplicas; i++) {
                var currentPort = 8079 + i;
                System.out.println("Connecting to " + currentPort);
                try {
                    var currentSocket = new Socket("localhost", currentPort);
                    var currentChannel = new ClientChannel(currentSocket, keyPair.getPrivate());
                    clientChannels.add(currentChannel);
                } catch (ConnectException e) {
                    System.out.println("Could not connect to " + currentPort + "...");
                }
            }

            BroadcastChannel broadcastChannel = new BroadcastChannel(clientChannels);
            register = new Register(broadcastChannel, quorumSize, key);

            TimeStampRequest timeStampRequest = new TimeStampRequest(KeyConversion.keyToString(keyPair.getPublic()));
            var gson = new Gson();
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "timeStamp");
            requestJson.add("request", JsonParser.parseString(gson.toJson(timeStampRequest)));
            JsonObject response = register.readTS(requestJson).get("response").getAsJsonObject();
            register.setTimestamp(new AtomicLong(response.get("ts").getAsLong()));

            broadcastChannel.closeSocket();

        }
        catch (Exception e){
            e.printStackTrace(System.err);
        }

        return register;
    }

    /**
     * Runs a given command using a given register. Used for JUNIT tests
     * @param register A register who will run the command.
     * @param command A command that will be ran.
     * @return the response of the command
     */

    public String runCommand(Register register, Command command) {
        System.out.println("Starting Client");

        try {
            ArrayList<ClientChannel> clientChannels = new ArrayList<>();

            for (int i = 1; i <= numberOfReplicas; i++) {
                var currentPort = 8079 + i;
                System.out.println("Connecting to " + currentPort);
                try {
                    var currentSocket = new Socket("localhost", currentPort);
                    var currentChannel = new ClientChannel(currentSocket, keyPair.getPrivate());
                    clientChannels.add(currentChannel);
                } catch (ConnectException e) {
                    System.out.println("Could not connect to " + currentPort + "...");
                }
            }

            BroadcastChannel broadcastChannel = new BroadcastChannel(clientChannels);
            register.setBroadcastChannel(broadcastChannel);
            register.setQuorumSize(quorumSize);

            String returned  = command.execCommand(register);

            broadcastChannel.closeSocket();

            return returned;

        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
}
