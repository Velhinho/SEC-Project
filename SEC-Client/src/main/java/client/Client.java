package client;

import client.commands.Command;
import client.commands.OpenCommand;
import com.google.gson.JsonObject;
import communication.channel.BadChannel;
import communication.channel.BroadcastChannel;
import communication.channel.Channel;
import communication.channel.ClientChannel;
import communication.crypto.KeyConversion;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Client {
    private static KeyPair getKeyPair(String arg0, String arg1, String arg2) throws RuntimeException {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            System.out.println(arg0 + arg2 + ".jks" );
            InputStream is = classloader.getResourceAsStream(arg0 + arg2 + ".jks" );
            ks.load(is, arg1.toCharArray());
            PrivateKey clientPrivateKey = (PrivateKey) ks.getKey("mykey", arg1.toCharArray());
            PublicKey clientPublicKey = ks.getCertificate("mykey").getPublicKey();
            return new KeyPair(clientPublicKey, clientPrivateKey);
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new RuntimeException(exception.getCause());
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Client");
        System.out.println("Args:" + Arrays.toString(args));


        try {
            var keyPair = getKeyPair(args[0], args[1], args[2]);
            System.out.println("My Key: " + KeyConversion.keyToString(keyPair.getPublic()));
            var quorumSize = 0;
            if(args[4] != null){
                quorumSize = Integer.parseInt(args[4]);
            }
            var numberOfReplicas = 1;
            if(args[5] != null){
                numberOfReplicas = Integer.parseInt(args[5]);
            }

            while (true) {

                System.out.println();
                System.out.println("Enter command");
                Command command = CommandParser.parseCommand();

                ArrayList<Channel> clientChannels = new ArrayList<>();

                for(int i = 1; i <= numberOfReplicas; i++){
                    var currentPort = 8079 + i;
                    System.out.println("Connecting to " + currentPort);
                    try {
                        var currentSocket = new Socket("localhost", currentPort);
                        var currentChannel = new ClientChannel(currentSocket, keyPair.getPrivate());
                        clientChannels.add(currentChannel);
                    }
                    catch (ConnectException e){
                        System.out.println("Could not connect to " + currentPort + "...");
                    }
                }

                BroadcastChannel broadcastChannel = new BroadcastChannel(clientChannels);
                Register register = new Register(broadcastChannel, quorumSize);

                command.execCommand(register);

                broadcastChannel.closeSocket();
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
