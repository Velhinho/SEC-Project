package client;

import communication.channel.BadChannel;
import communication.channel.ClientChannel;
import communication.crypto.KeyConversion;

import java.io.InputStream;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;

public class ClientOld {
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

            while (true) {
                try (var socket = new Socket("localhost", 8080)) {
                    if (Objects.equals(args[3], "yes")) {
                        var channel = new BadChannel(socket, keyPair.getPrivate());
                        var clientSide = new ClientSide(channel, keyPair.getPublic());
                        System.out.println();
                        System.out.println("Enter command");
                    } else {
                        var channel = new ClientChannel(socket, keyPair.getPrivate());
                        var clientSide = new ClientSide(channel, keyPair.getPublic());
                        System.out.println();
                        System.out.println("Enter command");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
