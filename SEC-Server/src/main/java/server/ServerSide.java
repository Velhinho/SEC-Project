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
import communication.messages.PendingTransfer;
import server.data.ServerData;
import communication.messages.AcceptedTransfer;

import java.security.KeyPair;
import java.util.*;

public class ServerSide {
    private final Channel channel;
    private final KeyPair keyPair;
    private final ServerData serverData;

    public Channel getChannel() {
        return channel;
    }

    public ServerSide(Channel channel, KeyPair keyPair, ServerData serverData) {
        this.channel = channel;
        this.keyPair = keyPair;
        this.serverData = serverData;
    }

    private JsonObject makeResponse(Object response) {
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        var key = KeyConversion.keyToString(keyPair.getPublic());
        responseJson.addProperty("key", key);
        return responseJson;
    }

    private JsonObject makeReadResponse(String response, long rid, long ts){
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(response));
        var key = KeyConversion.keyToString(keyPair.getPublic());
        responseJson.addProperty("key", key);
        responseJson.addProperty("readId", rid);
        responseJson.addProperty("ts", ts);
        return responseJson;
    }

    private JsonObject makeWriteResponse(Object response, long ts){
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        var key = KeyConversion.keyToString(keyPair.getPublic());
        responseJson.addProperty("key", key);
        responseJson.addProperty("ts", ts);
        return responseJson;
    }

    private static JsonObject getRequest(JsonObject jsonObject) {
        return jsonObject.get("request").getAsJsonObject();
    }

    private static long getWts(JsonObject jsonObject){
        System.out.println(jsonObject);
        return jsonObject.get("wts").getAsLong();
    }

    private static long getRid(JsonObject jsonObject){
        System.out.println(jsonObject);
        return jsonObject.get("readId").getAsLong();
    }

    public void processRequest() throws RuntimeException, ChannelException {
        var message = getChannel().receiveMessage();
        var signature = message.get("signature").getAsString();
        var requestJson = message.get("jsonObject").getAsJsonObject();
        var requestType = requestJson.get("requestType").getAsString();
        var gson = new Gson();

        if (Objects.equals(requestType, "checkAccount")) {
            var request = gson.fromJson(getRequest(requestJson), CheckAccountRequest.class);
            var rid = getRid(message);
            System.out.println("checkAccount: " + request);

            var response = checkAccount(request.getCheckKey());
            var ts = getAccountTs(request.getCheckKey());
            var responseJson = makeReadResponse(response, rid, ts);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(getRequest(requestJson), AuditRequest.class);
            var rid = getRid(message);
            System.out.println("audit: " + request);

            var response = audit(request.getAuditKey());
            var ts = getAccountTs(request.getAuditKey());
            var responseJson = makeReadResponse(response, rid, ts);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "openAccount")) {
            var request = gson.fromJson(getRequest(requestJson), OpenAccountRequest.class);
            System.out.println("openAccount: " + request);
            var stringResponse = openAccount(request.getKey());
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(getRequest(requestJson), SendAmountRequest.class);
            var wts = getWts(message);

            System.out.println("SendAmount: " + request);
            var ts = getAccountTs(request.getSender());
            var stringResponse = sendAmount(request.getSender(), request.getReceiver(), request.getAmount(), request.getKey(), wts, signature);
            var responseJson = makeWriteResponse(stringResponse, ts);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(getRequest(requestJson), ReceiveAmountRequest.class);
            var wts = getWts(message);
            System.out.println("receiveAmount: " + request);

            var stringResponse =  receiveAmount(request.getSender(), request.getReceiver(), request.getKey(), wts, signature);
            var responseJson = makeResponse(stringResponse);
            getChannel().sendMessage(responseJson);
        } else if (Objects.equals(requestType, "timeStamp")){
            var request = gson.fromJson(getRequest(requestJson), TimeStampRequest.class);
            var rid = getRid(message);
            System.out.println("timeStamp " + request);

            var stringResponse = getAccountTsResponse(request.getKey());
            var responseJson = makeReadResponse(stringResponse, rid, getAccountTs(request.getKey()));
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
        Gson gson = new Gson();
        String json;
        if( account != null){
            ArrayList<AcceptedTransfer> acceptedTransfers = account.getAcceptedTransfers();
            acceptedTransfers.sort(Comparator.comparing(AcceptedTransfer::getTimestamp));
            AuditResponse auditResponse = new AuditResponse(acceptedTransfers, "Audit");
            json = gson.toJson(auditResponse);
            return json;
        }
        ErrorResponse errorResponse = new ErrorResponse("Error", "Account with public key = " + publicKey + " does not exist");
        json = gson.toJson(errorResponse);
        return json;
    }

    private String sendAmount(String sender, String receiver, int amount, String key, long wts, String signature) {
        if (!key.equals(sender)){
            return "The signature of the request and the sender's key doesn't match!";
        }
        return serverData.sendAmount(sender, receiver, amount, wts, signature);
    }

    private String checkAccount(String publicKey){
        Account account = serverData.getAccount(publicKey);
        Gson gson = new Gson();
        String json;
        if( account != null){
            int balance = account.balance();
            ArrayList<PendingTransfer> pendingTransfers =  account.getPendingTransfers();
            pendingTransfers.sort(Comparator.comparing(d -> d.getTimestamp()));
            CheckResponse checkResponse = new CheckResponse(balance, pendingTransfers, "Check");
            json = gson.toJson(checkResponse);
            return json;
        }
        ErrorResponse errorResponse = new ErrorResponse("Error", "Account with public key = " + publicKey + " does not exist");
        json = gson.toJson(errorResponse);
        return json;
    }

    private String receiveAmount(String senderKey, String receiverKey, String key, long wts, String signature){
        if (!key.equals(receiverKey)){
            return "The signature of the request and the receiver's key doesn't match!";
        }
        return serverData.receiveAmount(senderKey, receiverKey, wts, signature);
    }

    private long getAccountTs(String publicKey){
        Account account = serverData.getAccount(publicKey);
        if( account != null){
            return account.getTs();
        }
        return 0;
    }

    private String getAccountTsResponse(String publicKey){
        Gson gson = new Gson();
        String json;
        TimeStampResponse timeStampResponse = new TimeStampResponse("timeStamp", getAccountTs(publicKey));
        json = gson.toJson(timeStampResponse);
        return json;
    }

}
