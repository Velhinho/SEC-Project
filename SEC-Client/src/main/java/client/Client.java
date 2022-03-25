package client;

import communication.channel.PlainChannel;
import communication.crypto.RSAKeyGenerator;

import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        System.out.println("Starting Client");

        try(var socket = new Socket("localhost", 8080)) {
            var channel = new PlainChannel(socket);
            var clientSide = new ClientSide(channel);
            var clientKeyPair = RSAKeyGenerator.generateKeyPair();
            var response = clientSide.checkAccount(clientKeyPair.getPublic());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
