package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.BroadcastChannel;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;
import communication.messages.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Register {
    private AtomicLong timestamp = new AtomicLong(0);
    private BroadcastChannel broadcastChannel;
    private int quorumSize;
    private boolean reading;
    private final int difficulty = 2;
    private final String publicKey;

    /**
     *
     * @param broadcastChannel
     * @param quorumSize
     */

    public Register(BroadcastChannel broadcastChannel, int quorumSize, String publicKey) {
        this.broadcastChannel = broadcastChannel;
        this.quorumSize = quorumSize;
        this.reading = false;
        this.publicKey = publicKey;
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

    /**
     *
     * @param jsonObject
     * @param timestamp
     * @param readId
     * @return
     */

    private static JsonObject makeWriteMsg(JsonObject jsonObject, long timestamp, long readId) {
        // SendAmountRequest = {"sender": ASD, "receiver": SDF, "amount": 1}
        // jsonWithTS = {"jsonObject": {"sender": ASD, "receiver": SDF, "amount": 1}, "wts": 3, "readId":2}

        var jsonWithTS = new JsonObject();
        jsonWithTS.addProperty("readId", readId);
        jsonWithTS.add("jsonObject", jsonObject);
        jsonWithTS.addProperty("wts", timestamp);
        return jsonWithTS;
    }

    /**
     *
     * @param jsonObject
     * @return
     */

    public String write(JsonObject jsonObject) {
        var timestamp = getTimestamp().incrementAndGet();
        System.out.println(timestamp);
        long readId = generateReadId();
        var writeMsg = makeWriteMsg(jsonObject, timestamp, readId);
        System.out.println(writeMsg);
        getBroadcastChannel().broadcastMsg(writeMsg);

        var acks = getBroadcastChannel().receiveMsgs();
        if (acks.size() < getQuorumSize()) {
            throw new RuntimeException("Not enough ACKs");
        }
        System.out.println(acks);
        if(!checkReadIds(acks, readId)){
            throw new RuntimeException("Wrong ReadIds received");
        }
        return acks.get(0).get("response").getAsString();
    }


    /**
    Generates a ReadId using SHA1PRNG and returns it
     @return the readId
     */
    private static long generateReadId() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     *
     * @param jsonObject
     * @param readId
     * @return
     */

    private static JsonObject makeReadMsg(JsonObject jsonObject, long readId) {
        // {"readId": 123}
        var jsonWithReadId = new JsonObject();
        jsonWithReadId.add("jsonObject", jsonObject);
        jsonWithReadId.addProperty("readId", readId);
        return jsonWithReadId;
    }

    /**
    * Verifies Signatures of an arraylist of JsonObjects
     * @param jsonObjects a list of JsonObjects
     * @return true if signatures are valid, false otherwise
     */

    private boolean verifySignatures(ArrayList<JsonObject> jsonObjects) {
        for (JsonObject jsonObject : jsonObjects) {
            System.out.println(jsonObject);
            JsonObject response = jsonObject.get("response").getAsJsonObject();
            String type = response.get("type").getAsString();
            if (type.equals("Check")) {
                if (!verifyCheckResponse(jsonObject)) {
                    return false;
                }
            } else if (type.equals("Audit")) {
                if (!verifyAuditResponse(jsonObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies of a response of an audit response is coherent with it's signature
     * @param jsonObject a jsonObject that is verified
     * @return true if the signature is valid, false otherwise
     */

    private boolean verifyAuditResponse(JsonObject jsonObject) {
        Gson gson = new Gson();
        AuditResponse auditResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), AuditResponse.class);
        ArrayList<AcceptedTransfer> acceptedTransfers = auditResponse.getTransfers();
        for (AcceptedTransfer acceptedTransfer : acceptedTransfers) {
            PublicKey sender = KeyConversion.stringToKey(acceptedTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(acceptedTransfer.receiver());
            ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest(sender, receiver);
            String signature = acceptedTransfer.getReceiverSignature();
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "receiveAmount");
            requestJson.add("request", JsonParser.parseString(gson.toJson(receiveAmountRequest)));
            JsonObject requestJsonWts = makeWriteMsg(requestJson, acceptedTransfer.getWts(), acceptedTransfer.getRid());
            try {
                if (!StringSignature.verify(requestJsonWts.toString(), signature, receiver)) {
                    System.out.println("Audit : Error on the Signatures");
                    return false;
                }
            } catch (CryptoException e) {
                System.exit(0);
            }
        }
        return true;
    }

    /**
     * Verifies of a response of an check response is coherent with it's signature
     * @param jsonObject a jsonObject that is verified
     * @return true if the signature is valid, false otherwise
     */

    private boolean verifyCheckResponse(JsonObject jsonObject) {
        Gson gson = new Gson();
        System.out.println(jsonObject);
        CheckResponse checkResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
        ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
        for (PendingTransfer pendingTransfer : pendingTransfers) {
            PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
            SendAmountRequest sendAmountRequest = new SendAmountRequest(sender, receiver, pendingTransfer.amount());
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "sendAmount");
            requestJson.add("request", JsonParser.parseString(gson.toJson(sendAmountRequest)));
            System.out.println(requestJson);
            JsonObject requestJsonWts = makeWriteMsg(requestJson, pendingTransfer.getWts(), pendingTransfer.getRid());
            System.out.println("requestJsonWts : "  + requestJsonWts);
            try {
                if (!StringSignature.verify(requestJsonWts.toString(), pendingTransfer.getSignature(), sender)) {
                    System.out.println("Check : Error on the Signatures");
                    return false;
                }
            } catch (CryptoException e) {
                System.exit(0);
            }
        }
        return true;
    }

    /**
     * Returns the jsonObject with the highest ts from a list of JsonObjects
     * @param jsonObjects a jsonObject that is verified
     * @return the JsonObject with the highest timestamp
     */

    private JsonObject highestValue(ArrayList<JsonObject> jsonObjects) {
        JsonObject maxJsonObject = null;
        long max_ts = -1;
        for (JsonObject currentJsonObject : jsonObjects) {
            long current_ts = currentJsonObject.get("ts").getAsLong();
            if (current_ts > max_ts) {
                max_ts = current_ts;
                maxJsonObject = currentJsonObject;
            }
        }
        return maxJsonObject;
    }

    /**
     * Verifies it the size given is larger or equal than the given quorum
     * @param size the size that is verified
     * @return True if the size is larger or equal, false if otherwise.
     */

    private boolean hasQuorumSize(int size) {
        return size >= getQuorumSize();
    }

    /**
     * Verifies if the readId of the given jsonObject matches the given readId
     * @param jsonObject a jsonObject with a readID
     * @param readId a long which is a readId
     * @return returns true if jsonObject's readId and the given readId matches, false if otherwise.
     */

    private static boolean checkReadId(JsonObject jsonObject, long readId) {
        // {"readId": 123, "jsonObject": ...}
        return jsonObject.get("readId").getAsLong() == readId;
    }

    /**
     *
     * @param jsonObjects
     * @param readId
     * @return
     */

    private boolean checkReadIds(ArrayList<JsonObject> jsonObjects, long readId) {
        var count = 0;
        for (var json : jsonObjects) {
            if (checkReadId(json, readId)) {
                count += 1;
            }
        }
        return hasQuorumSize(count);
    }

    /**
     *
     * @param object
     * @return
     */

    public String readOld(JsonObject object) {
        proofOfWork(object, difficulty);
        var readId = generateReadId();
        var readMsg = makeReadMsg(object, readId);
        reading = true;
        broadcastChannel.broadcastMsg(readMsg);
        var msgs = broadcastChannel.receiveMsgs();
        System.out.println(msgs);
        JsonObject highestValue;
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            highestValue = highestValue(msgs);
            ArrayList<JsonObject> necessaryMessages = getNecessaryMessages(highestValue);
            System.out.println("Has validated signatures!");
            for(JsonObject necessaryMessage : necessaryMessages){
                System.out.println("Sending Necessary Messages : " + necessaryMessage);
                readId = necessaryMessage.get("readId").getAsLong();
                broadcastChannel.broadcastDirectMsg(necessaryMessage);
                var acks = broadcastChannel.receiveMsgs();
                if (acks.size() < getQuorumSize()) {
                    throw new RuntimeException("Not enough ACKs");
                }
                if (!checkReadIds(acks, readId)) {
                    throw new RuntimeException("Wrong ReadIds received");
                }
            }
            reading = false;
            return highestValue.get("response").getAsString();
        } else {
            throw new RuntimeException("Too many crashes");
        }
    }

    public String read(JsonObject object) {
        proofOfWork(object, difficulty);
        var readId = generateReadId();
        var readMsg = makeReadMsg(object, readId);
        reading = true;
        broadcastChannel.broadcastMsg(readMsg);
        var msgs = broadcastChannel.receiveMsgs();
        System.out.println(msgs);
        JsonObject highestValue;
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            highestValue = highestValue(msgs);
            System.out.println("Has validated signatures!");
            JsonObject writebackMessage = null;
            if(object.get("requestType").getAsString().equals("audit")){
                writebackMessage = writeBackAudit(highestValue);
            }
            else if(object.get("requestType").getAsString().equals("checkAccount")){
                writebackMessage = writeBackCheck(highestValue);
            }
            if(writebackMessage != null){
                write(writebackMessage);
            }
            reading = false;
            System.out.println("highestValue: " + highestValue);
            JsonObject highestValueResponse = highestValue.get("response").getAsJsonObject();
            System.out.println("highestValueResponse: " + highestValueResponse);
            if(highestValueResponse.get("type").getAsString().equals("Error")){
                return highestValueResponse.get("errorMessage").getAsString();
            }
            if(highestValueResponse.get("type").getAsString().equals("Check")){
                Gson gson = new Gson();
                CheckResponse checkResponse = gson.fromJson(highestValueResponse, CheckResponse.class);
                return checkResponse.toString();
            }
            if(highestValueResponse.get("type").getAsString().equals("Audit")){
                Gson gson = new Gson();
                AuditResponse auditResponse = gson.fromJson(highestValueResponse, AuditResponse.class);
                return auditResponse.toString();
            }
            return highestValue.get("response").getAsString();
        } else {
            throw new RuntimeException("Too many crashes");
        }
    }

    /**
     *
     * @param jsonObject
     * @return
     */

    public ArrayList<JsonObject> getNecessaryMessages(JsonObject jsonObject){
        ArrayList<JsonObject> necessaryMessages = new ArrayList<>();
        JsonObject response = jsonObject.get("response").getAsJsonObject();
        String type = response.get("type").getAsString();
        Gson gson = new Gson();
        if (type.equals("Check")) {
            CheckResponse checkResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
            ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
            for(PendingTransfer pendingTransfer : pendingTransfers) {
                PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
                PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
                JsonObject sendAmountRequest = getSendAmountRequestMessage(pendingTransfer, sender, receiver, pendingTransfer.amount());
                sendAmountRequest.addProperty("signature", pendingTransfer.getSignature());
                necessaryMessages.add(sendAmountRequest);
            }
        }
        else if (type.equals("Audit")){
            AuditResponse auditResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), AuditResponse.class);
            ArrayList<AcceptedTransfer> acceptedTransfers = auditResponse.getTransfers();
            for(AcceptedTransfer acceptedTransfer : acceptedTransfers) {
                PublicKey sender = KeyConversion.stringToKey(acceptedTransfer.sender());
                PublicKey receiver = KeyConversion.stringToKey(acceptedTransfer.receiver());
                JsonObject receiverAmountRequest = getReceiveAmountRequestMessage(acceptedTransfer, sender, receiver);
                receiverAmountRequest.addProperty("signature", acceptedTransfer.getReceiverSignature());
                necessaryMessages.add(receiverAmountRequest);
            }
        }
        return necessaryMessages;
    }

    public JsonObject writeBackCheck(JsonObject jsonObject){
        ArrayList<JsonObject> necessaryMessages = new ArrayList<>();
        JsonObject response = jsonObject.get("response").getAsJsonObject();
        String type = response.get("type").getAsString();
        Gson gson = new Gson();
        WriteBackCheckRequest writeBackCheckRequest = new WriteBackCheckRequest(necessaryMessages, publicKey);
        if (type.equals("Check")) {
            CheckResponse checkResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
            ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
            for (PendingTransfer pendingTransfer : pendingTransfers) {
                PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
                PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
                JsonObject sendAmountRequest = getSendAmountRequestMessage(pendingTransfer, sender, receiver, pendingTransfer.amount());
                sendAmountRequest.addProperty("signature", pendingTransfer.getSignature());
                necessaryMessages.add(sendAmountRequest);
            }
            writeBackCheckRequest = new WriteBackCheckRequest(necessaryMessages, publicKey);
        }
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "writeBackCheck");
        requestJson.add("request", JsonParser.parseString(gson.toJson(writeBackCheckRequest)));
        return requestJson;
    }

    public JsonObject writeBackAudit(JsonObject jsonObject){
        ArrayList<JsonObject> necessaryMessages = new ArrayList<>();
        JsonObject response = jsonObject.get("response").getAsJsonObject();
        String type = response.get("type").getAsString();
        Gson gson = new Gson();
        WriteBackAuditRequest writeBackAuditRequest = new WriteBackAuditRequest(necessaryMessages, publicKey);
        if (type.equals("Check")) {
            AuditResponse auditResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), AuditResponse.class);
            ArrayList<AcceptedTransfer> acceptedTransfers = auditResponse.getTransfers();
            for (AcceptedTransfer acceptedTransfer : acceptedTransfers) {
                PublicKey sender = KeyConversion.stringToKey(acceptedTransfer.sender());
                PublicKey receiver = KeyConversion.stringToKey(acceptedTransfer.receiver());
                JsonObject receiveAmountRequest  = getReceiveAmountRequestMessage(acceptedTransfer, sender, receiver);
                receiveAmountRequest.addProperty("signature", acceptedTransfer.getReceiverSignature());
                necessaryMessages.add(receiveAmountRequest);
            }
            writeBackAuditRequest = new WriteBackAuditRequest(necessaryMessages, publicKey);
        }
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "writeBackAudit");
        requestJson.add("request", JsonParser.parseString(gson.toJson(writeBackAuditRequest)));
        return requestJson;
    }

    /**
     *
     * @param object
     * @return
     */

    public JsonObject readTS(JsonObject object) {
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

    /**
     *
     * @param pendingTransfer
     * @param sender
     * @param receiver
     * @param amount
     * @return
     */

    public JsonObject getSendAmountRequestMessage(PendingTransfer pendingTransfer, PublicKey sender, PublicKey receiver, int amount){
        Gson gson = new Gson();
        SendAmountRequest sendAmountRequest = new SendAmountRequest(sender, receiver, amount);
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "sendAmount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(sendAmountRequest)));
        return makeWriteMsg(requestJson, pendingTransfer.getWts(), pendingTransfer.getRid());
    }

    /**
     *
     * @param acceptedTransfer
     * @param sender
     * @param receiver
     * @return
     */

    public JsonObject getReceiveAmountRequestMessage(AcceptedTransfer acceptedTransfer, PublicKey sender, PublicKey receiver){
        Gson gson = new Gson();
        ReceiveAmountRequest receiveAmountRequest = new ReceiveAmountRequest(sender, receiver);
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "receiveAmount");
        requestJson.add("request", JsonParser.parseString(gson.toJson(receiveAmountRequest)));
        return makeWriteMsg(requestJson, acceptedTransfer.getWts(), acceptedTransfer.getRid());
    }


    /**
     * Does a proof of work puzzle using a JsonObject and with a certain difficulty.
     * @param jsonObject a jsonObject that is used as a basis for a proof of work
     * @param difficulty the difficulty of the proof of work
     * @return Returns the a byte array that is the proof of work
     */

    public static byte[] proofOfWork(JsonObject jsonObject, int difficulty){
        String messageString = jsonObject.toString();
        long nonce = 0;
        String stringToHash = messageString + nonce;
        try{
            final MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] stringBytes = stringToHash.getBytes();
            sha.update(stringBytes);
            byte[] hash = sha.digest();
            while (!matchesDifficulty(hash, difficulty)){
                nonce++;
                stringToHash = messageString + nonce;
                stringBytes = stringToHash.getBytes();
                sha.update(stringBytes);
                hash = sha.digest();
            }
            return hash;
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if a hash is a correct answer for a proof-of-work puzzle for a certain difficulty.
     * For example, if the hash is for example 000xxx and the difficulty is 3, the hash is a correct answer.
     * @param bytes the hash that needs to be verified
     * @param difficulty the difficulty of the proof of work
     * @return True if the hash is a correct answer for the proof of work, false if otherwise
     */

    public static boolean matchesDifficulty(byte [] bytes, int difficulty){
        if (difficulty > bytes.length){
            return false;
        }
        for(int i = 0; i < difficulty; i++){
            if(bytes[i] != 0){
                return false;
            }
        }
        return true;
    }

    public String getPublicKey() {
        return publicKey;
    }
}