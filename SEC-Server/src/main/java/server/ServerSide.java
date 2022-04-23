package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import communication.channel.ChannelException;
import communication.channel.ServerChannel;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;
import communication.messages.*;
import server.data.Account;
import communication.messages.PendingTransfer;
import server.data.ServerData;
import communication.messages.AcceptedTransfer;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.*;

public class ServerSide {
    private final ServerChannel channel;
    private final KeyPair keyPair;
    private final ServerData serverData;
    private boolean reading = false;

    public ServerChannel getChannel() {
        return channel;
    }

    public ServerSide(ServerChannel channel, KeyPair keyPair, ServerData serverData) {
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

    private JsonObject makeWriteResponse(Object response, long ts, long rid){
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        var key = KeyConversion.keyToString(keyPair.getPublic());
        responseJson.addProperty("key", key);
        responseJson.addProperty("readId", rid);
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
        System.out.println("signature: " + signature);
        var requestJson = message.get("jsonObject").getAsJsonObject();
        System.out.println("jsonObject: " + requestJson);
        var requestType = requestJson.get("requestType").getAsString();
        System.out.println("requestType: " + requestType);

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
            var rid = getRid(message);
            var ts = getAccountTs(request.getKey());

            System.out.println("openAccount: " + request);
            var stringResponse = openAccount(request.getKey());
            var responseJson = makeWriteResponse(stringResponse,ts,rid);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(getRequest(requestJson), SendAmountRequest.class);
            var wts = getWts(message);
            var rid = getRid(message);

            System.out.println("SendAmount: " + request);
            var stringResponse = sendAmount(request.getSender(), request.getReceiver(), request.getAmount(), request.getKey(), wts, signature, rid);
            var ts = getAccountTs(request.getSender());
            var responseJson = makeWriteResponse(stringResponse, ts, rid);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(getRequest(requestJson), ReceiveAmountRequest.class);
            var wts = getWts(message);
            var rid = getRid(message);
            System.out.println("receiveAmount: " + request);

            var stringResponse =  receiveAmount(request.getSender(), request.getReceiver(), request.getKey(), wts, signature, rid);
            var ts = getAccountTs(request.getReceiver());
            var responseJson = makeWriteResponse(stringResponse, ts, rid);
            getChannel().sendMessage(responseJson);
        } else if (Objects.equals(requestType, "timeStamp")){
            var request = gson.fromJson(getRequest(requestJson), TimeStampRequest.class);
            var rid = getRid(message);
            System.out.println("timeStamp: " + request);

            var stringResponse = getAccountTsResponse(request.getKey());
            var responseJson = makeReadResponse(stringResponse, rid, getAccountTs(request.getKey()));
            getChannel().sendMessage(responseJson);
        } else if(Objects.equals(requestType, "writeBackCheck")){
            var request = gson.fromJson(getRequest(requestJson), WriteBackCheckRequest.class);
            var wts = getWts(message);
            var rid = getRid(message);
            System.out.println("writeBackCheck: " + request);
            var stringResponse = writeBackCheck(request.getSendAmountRequests());
            var responseJson = makeWriteResponse(stringResponse, wts, rid);
            getChannel().sendMessage(responseJson);

        } else if(Objects.equals(requestType, "writeBackAudit")){
            var request = gson.fromJson(getRequest(requestJson), WriteBackAuditRequest.class);
            var wts = getWts(message);
            var rid = getRid(message);
            System.out.println("writeBackAudit: " + request);

            var stringResponse = writeBackAudit(request.getReceiveAmountRequests());
            var responseJson = makeWriteResponse(stringResponse, wts, rid);
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
            acceptedTransfers.sort(Comparator.comparing(AcceptedTransfer::getWts));
            AuditResponse auditResponse = new AuditResponse(acceptedTransfers, "Audit");
            json = gson.toJson(auditResponse);
            return json;
        }
        ErrorResponse errorResponse = new ErrorResponse("Error", "Account with public key = " + publicKey + " does not exist");
        json = gson.toJson(errorResponse);
        return json;
    }

    private String sendAmount(String sender, String receiver, int amount, String key, long wts, String signature, long rid) {
        if (!key.equals(sender)){
            return "The signature of the request and the sender's key doesn't match!";
        }
        if (sender.equals(receiver)) {
            return "Can't send money to itself!";
        }
        if (amount <= 0) {
            return "The amount of money sent needs to be higher than zero!";
        }
        return serverData.sendAmount(sender, receiver, amount, wts, signature, rid);
    }

    private String checkAccount(String publicKey){
        Account account = serverData.getAccount(publicKey);
        Gson gson = new Gson();
        String json;
        if( account != null){
            int balance = account.balance();
            ArrayList<PendingTransfer> pendingTransfers =  account.getPendingTransfers();
            pendingTransfers.sort(Comparator.comparing(Transfer::getWts));
            CheckResponse checkResponse = new CheckResponse(balance, pendingTransfers, "Check");
            json = gson.toJson(checkResponse);
            return json;
        }
        ErrorResponse errorResponse = new ErrorResponse("Error", "Account with public key = " + publicKey + " does not exist");
        json = gson.toJson(errorResponse);
        return json;
    }

    private String receiveAmount(String senderKey, String receiverKey, String key, long wts, String signature, long rid){
        if (!key.equals(receiverKey)){
            return "The signature of the request and the receiver's key doesn't match!";
        }
        if (senderKey.equals(receiverKey)) {
            return "You can't accept a transfer from yourself to yourself!";
        }
        return serverData.receiveAmount(senderKey, receiverKey, wts, signature, rid);
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

    private String writeBackCheck(ArrayList<JsonObject> jsonObjects){
        System.out.println(jsonObjects);
        System.out.println(jsonObjects.size());
        //jsonWithTS = {"jsonObject": { "request": Request, "requestType": type}, "wts": 3, "readId":2}
        for(JsonObject currentJsonObject : jsonObjects){

            long wts = currentJsonObject.get("wts").getAsLong();
            System.out.println("wts: " + wts);
            long readId = currentJsonObject.get("readId").getAsLong();
            System.out.println("readId: " + readId);
            String signature = currentJsonObject.get("signature").getAsString();
            System.out.println("signature: " + signature);
            JsonObject jsonObject = currentJsonObject.get("jsonObject").getAsJsonObject();
            System.out.println("jsonObject: "  + jsonObject);
            JsonObject jsonNoSignature = currentJsonObject.deepCopy();
            jsonNoSignature.remove("signature");
            System.out.println("jsonNoSignature: " + jsonNoSignature);
            JsonObject requestJson = jsonObject.get("request").getAsJsonObject();
            System.out.println("requestJson: " + requestJson);
            String type = jsonObject.get("requestType").getAsString();
            System.out.println("type: "+ type);
            if(type.equals("sendAmount")) {
                Gson gson = new Gson();
                SendAmountRequest sendAmountRequest = gson.fromJson(requestJson, SendAmountRequest.class);
                System.out.println("sendAmountRequest: " + sendAmountRequest);
                PublicKey publicKey = KeyConversion.stringToKey(sendAmountRequest.getSender());
                try {
                    if (StringSignature.verify(jsonNoSignature.toString(), signature, publicKey)) {
                        System.out.println("Starts Sending");
                        sendAmount(sendAmountRequest.getSender(), sendAmountRequest.getReceiver(), sendAmountRequest.getAmount(), sendAmountRequest.getKey(), wts, signature, readId);
                        System.out.println("Stops Sending");
                    }
                    else {
                        System.out.println("Transfer is badly signed");
                        return "Invalid Transfer Sent";
                    }
                }catch (CryptoException e){
                    System.out.println(e.getMessage());
                    return "Invalid Transfer Sent";
                }
            }
        }
        System.out.println("Reached the End");
        return "All Transactions processed";
    }

    private String writeBackAudit(ArrayList<JsonObject> jsonObjects){
        System.out.println(jsonObjects);
        Gson gson = new Gson();
        //jsonWithTS = {"jsonObject": { "request": Request, "requestType": type}, "wts": 3, "readId":2}
        for(JsonObject currentJsonObject : jsonObjects){
            long wts = currentJsonObject.get("wts").getAsLong();
            long readId = currentJsonObject.get("readId").getAsLong();
            String signature = currentJsonObject.get("signature").getAsString();
            JsonObject jsonObject = currentJsonObject.get("jsonObject").getAsJsonObject();
            JsonObject jsonNoSignature = currentJsonObject.deepCopy();
            jsonNoSignature.remove("signature");
            JsonObject requestJson = jsonObject.get("request").getAsJsonObject();
            if(jsonObject.get("requestType").getAsString().equals("receiveAmount")) {
                ReceiveAmountRequest receiveAmountRequest = gson.fromJson(requestJson, ReceiveAmountRequest.class);
                PublicKey publicKey = KeyConversion.stringToKey(receiveAmountRequest.getReceiver());
                try {
                    if (StringSignature.verify(jsonNoSignature.toString(), signature, publicKey)) {
                        receiveAmount(receiveAmountRequest.getSender(), receiveAmountRequest.getReceiver(), receiveAmountRequest.getKey(), wts, signature, readId);
                    }
                    else {
                        return "Invalid Transfer Sent";
                    }
                }catch (CryptoException e){
                    return "Invalid Transfer Sent";
                }
            }
        }
        return "All Transactions processed";
    }

}
