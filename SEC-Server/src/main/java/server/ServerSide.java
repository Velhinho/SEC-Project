package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.Channel;
import communication.channel.ChannelException;
import communication.messages.*;
//import server.data.ServerData;
import server.data.Account;
import server.data.ServerDataController;
import server.data.ServerDataDB;

import java.util.*;

public class ServerSide {
    private final Channel channel;

    private static ServerDataController serverData = new ServerDataController();

    public Channel getChannel() {
        return channel;
    }

    public ServerSide(Channel channel) {
        this.channel = channel;
    }



    private JsonObject makeResponse(Object response) {
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        return responseJson;
    }

    public void processRequest() throws RuntimeException, ChannelException {
        var requestJson = getChannel().receiveMessage();
        var requestType = requestJson.get("requestType").getAsString();
        var gson = new Gson();
        System.out.println("Json: " + requestJson);

        if (Objects.equals(requestType, "checkAccount")) {
            var request = gson.fromJson(requestJson.get("request"), CheckAccountRequest.class);
            System.out.println("checkAccount: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(requestJson.get("request"), AuditRequest.class);
            System.out.println("audit: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "openAccount")) {
            var request = gson.fromJson(requestJson.get("request"), OpenAccountRequest.class);

            System.out.println("openAccount: " + request);
            System.out.println("\n");
            openAccount(request.getPublicKey());

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(requestJson.get("request"), SendAmountRequest.class);
            System.out.println("SendAmount: " + request);
            //sendAmount(request.getSender(), request.getReceiver(), request.getAmount());

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(requestJson.get("request"), ReceiveAmountRequest.class);
            System.out.println("receiveAmount: " + request);
            //receiveAmount(request.getSender(), request.getReceiver());
        }
        else {
            throw new RuntimeException("invalid json message type");
        }
    }



    private void openAccount(String publicKey){
        serverData.openAccount(publicKey);
    }


    private String audit(String publicKey){
        Account account = serverData.getAccount(publicKey);
        if( account != null){
            ArrayList<Transfer> transfers = account.getTransfers();
            transfers.sort(Comparator.comparing(Transfer::getTimestamp));
            return "transfers = " + transfers;
        }
        return "Account with public key = " + publicKey + " does not exist";
    }

    private void sendAmount(String sender, String receiver, int amount) {
        serverData.sendAmount(sender, receiver, amount);
    }

    private String checkAccount(String publicKey){
        Account account = serverData.getAccount(publicKey);
        if( account != null){
            int balance = account.balance();
            ArrayList<PendingTransfer> pendingTransfers = account.getPendingTransfers();
            pendingTransfers.sort(Comparator.comparing(d -> d.transfer().getTimestamp()));
            return "Account Balance: " + balance + " \n" + pendingTransfers;
        }
        return "Account with public key = " + publicKey + " does not exist";
    }



    /*

    private void sendAmount(String sender, String receiver, int amount) {
        serverData.sendAmount(sender, receiver, amount);
    }

    private void receiveAmount(String sender, String receiver){
        serverData.receiveAmount(sender, receiver);
    }



    private String checkAccount(String publicKey){
        return "Account Balance: " + serverData.checkAccountBalance(publicKey) + " \n" + serverData.checkAccountTransfers(publicKey).toString();
    }

     */

}
