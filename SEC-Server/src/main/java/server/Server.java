package server;


import communication.channel.ChannelException;
import communication.channel.SignedChannel;
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
    private static void serveClient(Socket client, PrivateKey serverKey, PublicKey clientKey) throws RuntimeException {
        try {
            var channel = new SignedChannel(client, clientKey, serverKey);
            var serverSide = new ServerSide(channel);
            serverSide.processRequest();
        } catch (RuntimeException | ChannelException e) {
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
        var clientPublicKey = KeyConversion.stringToKey("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCYz19U" +
                "mtVZPR3Di3bzjS4mRqaTkQzH//mtg0AO2bDQEZcp3bw2ah91wyBv7vR6ZeWGQ5FqII/+2kHu7VIUNXpoF+Nrc8rP/mh4im" +
                "U6UGDGb5Pgk96o0eg2yX9q3u0g6U9BFBexN6/aHD5KQJXrzbV6AL6+vENvCmGIj3a5tCY/CQIDAQAB");

        var keyPair = getKeyPair(args[0], args[1]);
        System.out.println(KeyConversion.keyToString(keyPair.getPublic()));

        try(ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                System.out.println("Waiting for client");
                var client = serverSocket.accept();
                System.out.println("Accepted client");
                executorService.submit(() -> serveClient(client, keyPair.getPrivate(), clientPublicKey));
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
