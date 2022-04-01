package server;


import communication.channel.ChannelException;
import communication.channel.SignedChannel;
import communication.crypto.CryptoException;
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
    private static void serveClient(Socket client, PrivateKey serverKey) throws RuntimeException {
        try {
            var channel = new SignedChannel(client, serverKey);
            var serverSide = new ServerSide(channel);
            serverSide.receiveClientPublicKey();
            System.out.println(serverSide.getChannel().getPublicKey());
            while (!channel.getSocket().isClosed()) {
                serverSide.processRequest();
            }
        } catch (RuntimeException | ChannelException | IOException | CryptoException e) {
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
                executorService.submit(() -> serveClient(client, keyPair.getPrivate()));
            }
        } catch (IOException e) {
                e.printStackTrace(System.err);
        }
    }
}
