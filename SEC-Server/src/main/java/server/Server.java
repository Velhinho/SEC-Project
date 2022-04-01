package server;


import communication.channel.ChannelException;
import communication.channel.ServerChannel;
import communication.crypto.KeyConversion;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.Executors;

public class Server {
    private static void serveClient(Socket client, KeyPair keyPair) throws RuntimeException {
        try (client) {
            var channel = new ServerChannel(client, keyPair.getPrivate());
            var serverSide = new ServerSide(channel, keyPair);
            serverSide.processRequest();
        } catch (RuntimeException | ChannelException | IOException e) {
            e.printStackTrace(System.err);
            System.err.println(e.getMessage());
        }
    }

    private static KeyPair getKeyPair(String arg0, String arg1) throws RuntimeException {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream(arg0);
            ks.load(is, arg1.toCharArray());
            PrivateKey serverPrivateKey = (PrivateKey) ks.getKey("mykey", arg1.toCharArray());
            PublicKey serverPublicKey = ks.getCertificate("mykey").getPublicKey();
            return new KeyPair(serverPublicKey, serverPrivateKey);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getCause());
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Server");
        var executorService = Executors.newCachedThreadPool();

        var keyPair = getKeyPair(args[0], args[1]);
        System.out.println(KeyConversion.keyToString(keyPair.getPublic()));

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                System.out.println("Waiting for client");
                var client = serverSocket.accept();
                System.out.println("Accepted client");
                executorService.submit(() -> serveClient(client, keyPair));
            }
        } catch (IOException e) {
                e.printStackTrace(System.err);
        }
    }
}
