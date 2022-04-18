package client;

import client.commands.Command;
import communication.channel.ClientChannel;
import communication.crypto.KeyConversion;

import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class Client {

    public static ClientProcess clientProcess;

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

    public static Integer broadcast(int i, Command command, KeyPair keyPair){

        int port = 8079 + i;

        try (var socket = new Socket("localhost", port)) {
            var channel = new ClientChannel(socket, keyPair.getPrivate());
            var clientSide = new ClientSide(channel, keyPair.getPublic());
            command.execCommand(clientSide);
            return 1;
        }
        catch (Exception e){
            e.printStackTrace(System.err);
            return 0;
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Client");
        System.out.println("Args:" + Arrays.toString(args));
        var executorService = Executors.newCachedThreadPool();

        if (args.length > 4){
            System.out.println("Define the clientNumber, badChannel and numberOfReplicas with -DclientNumber, -DbadChannel and -DnumberOfReplicas respectivelyl");
        }

        clientProcess = new ClientProcess();

        try {
            var keyPair = getKeyPair(args[0], args[1], args[2]);
            System.out.println("My Key: " + KeyConversion.keyToString(keyPair.getPublic()));
            Command command;
            int numberOfReplicas = Integer.parseInt(args[4]);

            List<Callable<Integer>> threads = new ArrayList<>();
            List<Future<Integer>> acks;
            int total_acks;

            while (true) {
                total_acks = 0;
                System.out.println();
                System.out.println("Enter command");
                command =  CommandParser2.parseCommand();


                //Beginning of algorithm

                if (command == null){
                    System.exit(0);
                }

                if (command.getType().equals("Read")){
                    //Rid = rid + 1
                    clientProcess.setRid(clientProcess.getRid() + 1);
                    //readlist = [NULL]^N
                    clientProcess.setReadlist(new ArrayList<>());

                }
                else if(command.getType().equals("Write")){
                    //Wts = wts + 1
                    clientProcess.setWts(clientProcess.getWts() + 1);
                    //acklist = [NULL]^N
                    clientProcess.setAckslist(new ArrayList<>());
                }

                for (int i = 1; i <= numberOfReplicas ; i++) {
                    int finalI = i;
                    Command finalCommand = command;
                    threads.add(() -> broadcast(finalI, finalCommand, keyPair));
                        //executorService.submit(() -> broadcast(finalI, finalCommand, keyPair));
                }
                //acks = executorService.invokeAll(threads, 2000, TimeUnit.SECONDS);
                acks = executorService.invokeAll(threads);
                while(total_acks >= numberOfReplicas/2) {
                    total_acks = 0;
                    for (Future<Integer> ack : acks) {
                        if (ack.isDone()) {
                            total_acks += ack.get();
                        }
                    }
                }
                threads.clear();
                acks.clear();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
