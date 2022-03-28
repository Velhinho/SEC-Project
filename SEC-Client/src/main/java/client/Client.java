package client;

import communication.channel.PlainChannel;

import java.io.InputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.regex.Pattern;

public class Client {

    public static void main(String[] args) {
        System.out.println("Starting Client");

        try(var socket = new Socket("localhost", 8080)) {
            var channel = new PlainChannel(socket);
            var clientSide = new ClientSide(channel);
            System.out.println("Enter command");
            CommandParser.parseCommand(clientSide);

            KeyStore ks = KeyStore.getInstance("JKS");
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("clientKeys.jks");
            ks.load(is, args[0].toCharArray());
            PrivateKey clientPrivateKey = (PrivateKey) ks.getKey("mykey", args[0].toCharArray());
            PublicKey clientPublicKey = ks.getCertificate("mykey").getPublicKey();
            var clientKeyPair = new KeyPair(clientPublicKey, clientPrivateKey);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
