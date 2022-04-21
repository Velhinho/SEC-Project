package server;


import communication.channel.ChannelException;
import communication.channel.ServerChannel;
import communication.crypto.KeyConversion;
import server.data.ServerData;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.Executors;

public class ServerTest {

    private final KeyPair keyPair;
    private final int replicaNumber;
    private final String reset;

    public ServerTest(KeyPair keyPair, int replicaNumber, String reset){
        this.keyPair = keyPair;
        this.replicaNumber = replicaNumber;
        this.reset = reset;
    }

    private void serveClient(Socket client, KeyPair keyPair, ServerData serverData) throws RuntimeException {
        try (client) {
            var channel = new ServerChannel(client, keyPair.getPrivate());
            var serverSide = new ServerSide(channel, keyPair, serverData);
            serverSide.processRequest();
        } catch (RuntimeException | ChannelException | IOException e) {
            e.printStackTrace(System.err);
            System.err.println(e.getMessage());
        }
    }

    private KeyPair getKeyPair(String arg0, String arg1) throws RuntimeException {
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

    public void run(String[] args) {
        System.out.println("Starting Server");
        var executorService = Executors.newCachedThreadPool();

        int port = 8079 + replicaNumber;

        ServerData serverData = new ServerData(replicaNumber, reset);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Waiting for client on port " + port);
                var client = serverSocket.accept();
                System.out.println("Accepted client");
                executorService.submit(() -> serveClient(client, keyPair, serverData));
            }
        } catch (IOException e) {
                e.printStackTrace(System.err);
        }
    }
}
