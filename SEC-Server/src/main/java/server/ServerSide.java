package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.Channel;
import communication.channel.ChannelException;
import communication.crypto.KeyConversion;
import communication.messages.*;
//import server.data.ServerData;
import server.data.Account;
import server.data.PendingTransfer;
import server.data.ServerData;
import server.data.Transfer;

import java.security.KeyPair;
import java.util.*;

public class ServerSide {
    private final Channel channel;
    private final KeyPair keyPair;
    //private static ServerDataController serverData = new ServerDataController();
    private static ServerData serverData = new ServerData();

    public Channel getChannel() {
        return channel;
    }

    public ServerSide(Channel channel, KeyPair keyPair) {
        this.channel = channel;
        this.keyPair = keyPair;
    }

    private JsonObject makeResponse(Object response) {
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        var key = KeyConversion.keyToString(keyPair.getPublic());
        responseJson.addProperty("key", key);
        return responseJson;
    }

    private static JsonObject getRequest(JsonObject jsonObject) {
        return jsonObject.get("request").getAsJsonObject();
    }

    public void processRequest() throws RuntimeException, ChannelException {
        var requestJson = getChannel().receiveMessage();
        var requestType = requestJson.get("requestType").getAsString();
        var gson = new Gson();

        if (Objects.equals(requestType, "checkAccount")) {
            var request = gson.fromJson(getRequest(requestJson), CheckAccountRequest.class);
            System.out.println("checkAccount: " + request);

            var response = checkAccount(request.getCheckKey());
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(getRequest(requestJson), AuditRequest.class);
            System.out.println("audit: " + request);

            var response = audit(request.getAuditKey());
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "openAccount")) {
            var request = gson.fromJson(getRequest(requestJson), OpenAccountRequest.class);
            System.out.println("openAccount: " + request);
            var stringResponse = openAccount(request.getKey());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(getRequest(requestJson), SendAmountRequest.class);
            System.out.println("SendAmount: " + request);

            var stringResponse = sendAmount(request.getSender(), request.getReceiver(), request.getAmount(), request.getKey());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(getRequest(requestJson), ReceiveAmountRequest.class);
            System.out.println("receiveAmount: " + request);

            var stringResponse =  receiveAmount(request.getSender(), request.getReceiver(), request.getKey());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);
        }
        else {
            throw new RuntimeException("invalid json message type");
        }
    }

    private String openAccount(String publicKey){
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

    private String sendAmount(String sender, String receiver, int amount, String key) {
        if (!key.equals(sender)){
            return "The signature of the request and the sender's key doesn't match!";
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

    private String receiveAmount(String senderKey, String receiverKey, String key){
        if (!key.equals(receiverKey)){
            return "The signature of the request and the receiver's key doesn't match!";
        }
        return serverData.receiveAmount(senderKey, receiverKey);
    }

}
