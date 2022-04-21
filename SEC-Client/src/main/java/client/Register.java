package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.BroadcastChannel;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;
import communication.messages.*;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Register {
    private AtomicLong timestamp = new AtomicLong(0);
    private BroadcastChannel broadcastChannel;
    private int quorumSize;
    private boolean reading = false;

    public Register(BroadcastChannel broadcastChannel, int quorumSize) {
        this.broadcastChannel = broadcastChannel;
        this.quorumSize = quorumSize;
        this.reading = false;
    }

    public void setBroadcastChannel(BroadcastChannel broadcastChannel) {
        this.broadcastChannel = broadcastChannel;
    }

    public void setQuorumSize(int quorumSize) {
        this.quorumSize = quorumSize;
    }

    public AtomicLong getTimestamp() {
        return timestamp;
    }

    public BroadcastChannel getBroadcastChannel() {
        return broadcastChannel;
    }

    public int getQuorumSize() {
        return quorumSize;
    }

    private static JsonObject makeWriteMsg(JsonObject jsonObject, long timestamp) {
        // SendAmountRequest = {"sender": ASD, "receiver": SDF, "amount": 1}
        // jsonWithTS = {"jsonObject": {"sender": ASD, "receiver": SDF, "amount": 1}, "wts": 3}

        var jsonWithTS = new JsonObject();
        jsonWithTS.add("jsonObject", jsonObject);
        jsonWithTS.addProperty("wts", timestamp);
        return jsonWithTS;
    }

    public String write(JsonObject jsonObject) {
        var timestamp = getTimestamp().incrementAndGet();
        System.out.println(timestamp);
        var writeMsg = makeWriteMsg(jsonObject, timestamp);
        System.out.println(writeMsg);
        getBroadcastChannel().broadcastMsg(writeMsg);

        var acks = getBroadcastChannel().receiveMsgs();
        if (acks.size() <= getQuorumSize()) {
            throw new RuntimeException("Not enough ACKs");
        }
        return acks.get(0).get("response").getAsString();
    }

    private static long generateReadId() {
        try {
            var rng = SecureRandom.getInstance("SHA1PRNG");
            return rng.nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static JsonObject makeReadMsg(JsonObject jsonObject, long readId) {
        // {"readId": 123}
        var jsonWithReadId = new JsonObject();
        jsonWithReadId.add("jsonObject", jsonObject);
        jsonWithReadId.addProperty("readId", readId);
        return jsonWithReadId;
    }

    private boolean verifySignatures(ArrayList<JsonObject> jsonObjects) {
        for(JsonObject jsonObject : jsonObjects){
            System.out.println(jsonObject);
            JsonObject response = jsonObject.get("response").getAsJsonObject();
            String type = response.get("type").getAsString();
            if(type.equals("Check")){
                if(!verifyCheckResponse(jsonObject)){
                    return false;
                }
            }
            else if(type.equals("Audit")){
                if(!verifyAuditResponse(jsonObject)){
                    return false;
                }
            }
        }
        return true;
    }

    private boolean verifyAuditResponse(JsonObject jsonObject) {
        Gson gson = new Gson();
        AuditResponse auditResponse =  gson.fromJson(jsonObject.get("response").getAsJsonObject(), AuditResponse.class);
        ArrayList<AcceptedTransfer> acceptedTransfers = auditResponse.getTransfers();
        for(AcceptedTransfer acceptedTransfer : acceptedTransfers){
            PublicKey sender = KeyConversion.stringToKey(acceptedTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(acceptedTransfer.receiver());
            ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest(sender, receiver);
            String signature = acceptedTransfer.getSignature();
            long wts = acceptedTransfer.getWts();
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "receiveAmount");
            requestJson.add("request", JsonParser.parseString(gson.toJson(receiveAmountRequest)));
            JsonObject requestJsonWts = makeWriteMsg(requestJson, wts);
            try {
                if (!StringSignature.verify(requestJsonWts.toString(), signature, receiver)) {
                    return false;
                }
            }
            catch (CryptoException e){
                System.exit(0);
            }
        }
        return true;
    }

    private boolean verifyCheckResponse(JsonObject jsonObject) {
        Gson gson = new Gson();
        System.out.println(jsonObject);
        CheckResponse checkResponse =  gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
        ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
        System.out.println("pending_transfers : " + pendingTransfers);
        System.out.println("balance : " + checkResponse.getBalance());
        for(PendingTransfer pendingTransfer : pendingTransfers){
            PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
            SendAmountRequest sendAmountRequest = new SendAmountRequest(sender, receiver, pendingTransfer.amount());
            String signature = pendingTransfer.getSignature();
            long wts = pendingTransfer.getWts();
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "sendAmount");
            requestJson.add("request", JsonParser.parseString(gson.toJson(sendAmountRequest)));
            JsonObject requestJsonWts = makeWriteMsg(requestJson, wts);
            try {
                if (!StringSignature.verify(requestJsonWts.toString(), signature, sender)) {
                    return false;
                }
            }
            catch (CryptoException e){
                System.exit(0);
            }
        }
        return true;
    }

    private JsonObject highestValue(ArrayList<JsonObject> jsonObjects) {
        JsonObject maxJsonObject = null;
        long max_ts = -1;
        for(JsonObject currentJsonObject : jsonObjects){
            long current_ts = currentJsonObject.get("ts").getAsLong();
            if(current_ts > max_ts){
                max_ts = current_ts;
                maxJsonObject = currentJsonObject;
            }
        }
        return maxJsonObject;
    }

    private boolean hasQuorumSize(int size) {
        return size >= getQuorumSize();
    }

    private static boolean checkReadId(JsonObject jsonObject, long readId) {
        // {"readId": 123, "jsonObject": ...}
        return jsonObject.get("readId").getAsLong() == readId;
    }

    private boolean checkReadIds(ArrayList<JsonObject> jsonObjects, long readId) {
        var count = 0;
        for (var json : jsonObjects) {
            if (checkReadId(json, readId)) {
                count += 1;
            }
        }
        return hasQuorumSize(count);
    }

    public JsonObject read(JsonObject object) {
        var readId = generateReadId();
        var readMsg = makeReadMsg(object, readId);
        reading = true;
        broadcastChannel.broadcastMsg(readMsg);

        var msgs = broadcastChannel.receiveMsgs();
        System.out.println(msgs);
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            return highestValue(msgs);
        } else {
            throw new RuntimeException("Too many crashes");
        }
    }

    public void setTimestamp(AtomicLong timestamp) {
        this.timestamp = timestamp;
    }

    public void getMissingTransfers(JsonObject jsonObjectMaxTs, JsonObject otherJsonObject){
        JsonObject response = jsonObjectMaxTs.get("response").getAsJsonObject();
        String type = response.get("type").getAsString();
        ArrayList<JsonObject> maxTSTransfers = new ArrayList<>();
        if(type.equals("Audit")) {
            maxTSTransfers = getAuditJsonObjects(jsonObjectMaxTs);
        }
        else if(type.equals("Check")){
            maxTSTransfers = getCheckJsonObjects(jsonObjectMaxTs);
        }
        JsonObject otherResponse = otherJsonObject.get("response").getAsJsonObject();
        String otherType = otherResponse.get("type").getAsString();
        ArrayList<JsonObject> otherTSTransfers = new ArrayList<>();
        if(otherType.equals("Audit")) {
            otherTSTransfers = getAuditJsonObjects(jsonObjectMaxTs);
        }
        else if(otherType.equals("Check")){
            otherTSTransfers = getCheckJsonObjects(jsonObjectMaxTs);
        }
        



    }

    public JsonObject getReceiveAmountRequestMessage(AcceptedTransfer acceptedTransfer, PublicKey sender, PublicKey receiver){
        Gson gson = new Gson();
        ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest(sender, receiver);
        JsonObject requestJson = new JsonObject();
        long wts = acceptedTransfer.getWts();
        requestJson.addProperty("requestType", "receiveAmount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(receiveAmountRequest)));
        JsonObject requestJsonWts = makeWriteMsg(requestJson, wts);
        return requestJsonWts;
    }

    public ArrayList<JsonObject> getAuditJsonObjects(JsonObject jsonObject){
        Gson gson = new Gson();
        ArrayList<JsonObject> maxTSTransfers = new ArrayList<>();
        AuditResponse auditResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), AuditResponse.class);
        ArrayList<AcceptedTransfer> acceptedTransfers = auditResponse.getTransfers();
        for(AcceptedTransfer acceptedTransfer : acceptedTransfers) {
            PublicKey sender = KeyConversion.stringToKey(acceptedTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(acceptedTransfer.receiver());
            JsonObject requestJsonWts = getReceiveAmountRequestMessage(acceptedTransfer, sender, receiver);
            maxTSTransfers.add(requestJsonWts);
        }
        return maxTSTransfers;
    }

    public ArrayList<JsonObject> getCheckJsonObjects(JsonObject jsonObject){
        Gson gson = new Gson();
        ArrayList<JsonObject> maxTSTransfers = new ArrayList<>();
        CheckResponse checkResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
        ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
        for(PendingTransfer pendingTransfer : pendingTransfers) {
            PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
            JsonObject requestJsonWts = getSendAmountRequestMessage(pendingTransfer, sender, receiver, pendingTransfer.amount()
            );
            maxTSTransfers.add(requestJsonWts);
        }
        return maxTSTransfers;
    }

    public JsonObject getSendAmountRequestMessage(PendingTransfer pendingTransfer, PublicKey sender, PublicKey receiver, int amount){
        Gson gson = new Gson();
        SendAmountRequest sendAmountRequest = new SendAmountRequest(sender, receiver, amount);
        JsonObject requestJson = new JsonObject();
        long wts = pendingTransfer.getWts();
        requestJson.addProperty("requestType", "sendAmount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(sendAmountRequest)));
        JsonObject requestJsonWts = makeWriteMsg(requestJson, wts);
        return requestJsonWts;
    }

}
