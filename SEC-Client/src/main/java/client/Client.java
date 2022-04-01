package client;

import communication.channel.PlainChannel;
import communication.channel.SignedChannel;
import communication.crypto.KeyConversion;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;

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



        try(var socket = new Socket("localhost", 8080)) {
            var keyPair = getKeyPair(args[0], args[1], args[2]);
            System.out.println(KeyConversion.keyToString(keyPair.getPublic()));
            var serverPublicKey = KeyConversion.stringToKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDR5XF" +
                    "Qum9i0YS5clSPUpc4tVsd/fr383tXqhEu3+vYAi0ORqFQ/7h6ZlSH66xO6etg9Z1reyjsSo81t9rt1jg8Jo3JGhDf053e" +
                    "8KDXr9HJgqLSZPi1VJtlvJV4jZ4xBdKtsG0A95XA/CeA3JaQB8ZmV5mY8qj/SRIWanS4JT7kzQIDAQAB");

            var channel = new SignedChannel(socket, serverPublicKey, keyPair.getPrivate());
            var clientSide = new ClientSide(channel, keyPair.getPublic());


            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run(){
                    try {
                        System.out.println("Ctrl+C detected. Shutting down the Client.");
                        channel.closeSocket();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


            if(clientSide.sendPublicKey()) {
                System.out.println("SHID");
                while (true) {
                    System.out.println("Enter command");
                    CommandParser.parseCommand(clientSide);
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
