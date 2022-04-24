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

public class Server {

    /**
     * Creates a thread that serves the client while the socket is open.
     * @param client The socket of the client
     * @param keyPair The keys of the server
     * @param serverData The database where the client's request will be executed
     * @throws RuntimeException
     */


    private static void serveClient(Socket client, KeyPair keyPair, ServerData serverData) throws RuntimeException {
        try (client) {
            var channel = new ServerChannel(client, keyPair.getPrivate());
            var serverSide = new ServerSide(channel, keyPair, serverData);
            while (!serverSide.getChannel().getSocket().isClosed()) {
                try {
                    serverSide.processRequest();
                }
                catch (NullPointerException e){
                    serverSide.getChannel().getSocket().close();
                }
            }
        } catch (RuntimeException | ChannelException | IOException e) {
            e.printStackTrace(System.err);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets a KeyPair from a .jks file
     * @param arg0 The name of the .jks file
     * @param arg1 The password of the .jks file
     * @param arg2 The number of the .jks file
     * @return a KeyPair obtained from the .jks file
     * @throws RuntimeException
     */

    private static KeyPair getKeyPair(String arg0, String arg1, String arg2) throws RuntimeException {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            System.out.println(arg0 + arg2 + ".jks" );
            InputStream is = classloader.getResourceAsStream(arg0 + arg2 + ".jks" );
            ks.load(is, arg1.toCharArray());
            PrivateKey serverPrivateKey = (PrivateKey) ks.getKey("mykey", arg1.toCharArray());
            PublicKey serverPublicKey = ks.getCertificate("mykey").getPublicKey();
            return new KeyPair(serverPublicKey, serverPrivateKey);
        } catch (Exception exception) {
            throw new RuntimeException(exception.getCause());
        }
    }

    /**
     * Starts a server, which will create threads to serve clients
     * @param args The args are:
     *            args[0] = name of the .jks file
     *            args[1] = password of the .jks file
     *            args[2] = number of the replica (If null, then we assume we are using replica 1)
     *            args[3] = If the database needs to be reset or not (If null, then we assume we do not reset)
     */

    public static void main(String[] args) {
        System.out.println("Starting Server");
        var executorService = Executors.newCachedThreadPool();

        int replicaNumber = 1;

        if(args[2] != null){
            replicaNumber = Integer.parseInt(args[2]);
        }

        var keyPair = getKeyPair(args[0], args[1], Integer.toString(replicaNumber));
        System.out.println(KeyConversion.keyToString(keyPair.getPublic()));

        String reset = "no";

        if(args[3] != null){
            reset = args[3];
        }

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
