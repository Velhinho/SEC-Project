package server;


import communication.channel.ChannelException;
import communication.channel.PlainChannel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

public class Server {
    private static void serveClient(Socket client) throws RuntimeException {
        try {
            var channel = new PlainChannel(client);
            var serverSide = new ServerSide(channel);
            serverSide.processRequest();
        } catch (RuntimeException | ChannelException e) {
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Server");
        var executorService = Executors.newCachedThreadPool();

        try(ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                System.out.println("Waiting for client");
                var client = serverSocket.accept();
                System.out.println("Accepted client");
                executorService.submit(() -> serveClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}
