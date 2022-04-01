package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.ChannelException;
import communication.channel.SignedChannel;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;
import communication.messages.*;
//import server.data.ServerData;
import server.data.Account;
import server.data.ServerDataControllerTransactions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public class ServerSide {
    private final SignedChannel channel;

    //private static ServerDataController serverData = new ServerDataController();
    private static ServerDataControllerTransactions serverData = new ServerDataControllerTransactions();

    public SignedChannel getChannel() {
        return channel;
    }

    public ServerSide(SignedChannel channel) {
        this.channel = channel;
    }

    public void receiveClientPublicKey() throws IOException, CryptoException {
        var reader = new BufferedReader(new InputStreamReader(channel.getSocket().getInputStream()));
        var publicKey = reader.readLine();
        channel.setPublicKey(KeyConversion.stringToKey(publicKey));
        String response = "Key Passed With Success";
        var signature = StringSignature.sign(response, channel.getPrivateKey());
        var writer = new PrintWriter(channel.getSocket().getOutputStream());
        writer.println(signature);
        writer.flush();
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

            var response = checkAccount(request.key());
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(requestJson.get("request"), AuditRequest.class);
            System.out.println("audit: " + request);

            var response = audit(request.key());
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "openAccount")) {
            var request = gson.fromJson(requestJson.get("request"), OpenAccountRequest.class);
            System.out.println("openAccount: " + request);
            var stringResponse = openAccount(request.getKey());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(requestJson.get("request"), SendAmountRequest.class);
            System.out.println("SendAmount: " + request);

            var stringResponse = sendAmount(request.getSender(), request.getReceiver(), request.getAmount());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(requestJson.get("request"), ReceiveAmountRequest.class);
            System.out.println("receiveAmount: " + request);

            var stringResponse =  receiveAmount(request.getSender(), request.getReceiver());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);
        }
        else {
            throw new RuntimeException("invalid json message type");
        }
    }

    private String openAccount(String publicKey){
        if (!publicKey.equals(KeyConversion.keyToString(channel.getPublicKey()))){
            return "Unauthorized Operation. Can only create an account with your Public Key";
        }
        Account account = serverData.getAccount(publicKey);
        if (account == null){
            serverData.openAccount(publicKey);
            return "Account Opened With Success!";
        }
        else{
            return "Account with Public Key = " + publicKey + "already exists";
        }
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

    private String sendAmount(String sender, String receiver, int amount) {
        if (!sender.equals(KeyConversion.keyToString(channel.getPublicKey()))){
            return "Unauthorized Operation. Can only send money from accounts with Public Key associated to yours.";
        }
        return serverData.sendAmount(sender, receiver, amount);
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

    private String receiveAmount(String senderKey, String receiverKey){
        if (!receiverKey.equals(KeyConversion.keyToString(channel.getPublicKey()))){
            return "Unauthorized Operation. Can only accept money to an account with Public Key associated to yours.";
        }
        return serverData.receiveAmount(senderKey, receiverKey);
    }

}
