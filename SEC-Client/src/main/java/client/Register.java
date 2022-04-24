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
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


public class Register {
    private AtomicLong timestamp = new AtomicLong(0);
    private BroadcastChannel broadcastChannel;
    private int quorumSize;
    private boolean reading;
    private final int difficulty = 2;
    private final String publicKey;

    /**
     * Constructor for the register
     * @param broadcastChannel a broadcast channel
     * @param quorumSize a quorumSize
     * @param publicKey a publicKey
     */

    public Register(BroadcastChannel broadcastChannel, int quorumSize, String publicKey) {
        this.broadcastChannel = broadcastChannel;
        this.quorumSize = quorumSize;
        this.reading = false;
        this.publicKey = publicKey;
    }

    /**
     * Setter for the broadcast channel
     * @param broadcastChannel the broadcast channel we want.
     */

    public void setBroadcastChannel(BroadcastChannel broadcastChannel) {
        this.broadcastChannel = broadcastChannel;
    }

    /**
     * A setter for the quorum size
     * @param quorumSize the quorum size we want
     */

    public void setQuorumSize(int quorumSize) {
        this.quorumSize = quorumSize;
    }

    /**
     * A getter for the timestamp
     * @return the timestamp
     */

    public AtomicLong getTimestamp() {
        return timestamp;
    }

    /**
     * A getter for the broadcast channel
     * @return the broadcast channel
     */

    public BroadcastChannel getBroadcastChannel() {
        return broadcastChannel;
    }

    /**
     * A getter for the quorum size
     * @return the quorum size
     */

    public int getQuorumSize() {
        return quorumSize;
    }

    /**
     * Adds the properties of wts and readId to a JsonObject
     * @param jsonObject the JsonObject
     * @param timestamp the wt
     * @param readId the readId
     * @return A JsonObject with the properties of wts and readId added
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
     * Broadcast a write operation and receives acks.
     * @param jsonObject A JsonObject containing a write operation(send or receive).
     * @return The response of the server
     */

    public String write(JsonObject jsonObject) {
        var timestamp = getTimestamp().incrementAndGet();
        long readId = generateReadId();
        var writeMsg = makeWriteMsg(jsonObject, timestamp, readId);
        getBroadcastChannel().broadcastMsg(writeMsg);

        var acks = getBroadcastChannel().receiveMsgs();
        if (acks.size() < getQuorumSize()) {
            throw new RuntimeException("Not enough ACKs");
        }
        if(!checkReadIds(acks, readId)){
            throw new RuntimeException("Wrong ReadIds received");
        }
        ArrayList<String> acksResponse = new ArrayList<>(acks.stream().map(d -> d.get("response").getAsString()).collect(Collectors.toList()));
        var quorumResponses = getQuorumResponses(acksResponse);
        if(quorumResponses.size() == 0){
            throw new RuntimeException("Not enough ACKs");
        }
        return quorumResponses.get(0);
    }

    /**
     * Generates a ReadId using SHA1PRNG and returns it
     * @return the readId
     */

    private static long generateReadId() {
        try {
            return SecureRandom.getInstance("SHA1PRNG").nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Returns a jsonObject with the added property of the readId
     * @param jsonObject the original JsonObject
     * @param readId the readId that will be added
     * @return a JsonObject with the readId added.
     */

    private static JsonObject makeReadMsg(JsonObject jsonObject, long readId) {
        // {"readId": 123}
        var jsonWithReadId = new JsonObject();
        jsonWithReadId.add("jsonObject", jsonObject);
        jsonWithReadId.addProperty("readId", readId);
        return jsonWithReadId;
    }

    /** Verifies Signatures of an arraylist of JsonObjects
     * @param jsonObjects a list of JsonObjects
     * @return true if signatures are valid, false otherwise
     */

    private boolean verifySignatures(ArrayList<JsonObject> jsonObjects) {
        for (JsonObject jsonObject : jsonObjects) {
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

    /** Verifies of a response of an audit response is coherent with it's signature
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

    /** Verifies of a response of an check response is coherent with it's signature
     * @param jsonObject a jsonObject that is verified
     * @return true if the signature is valid, false otherwise
     */

    private boolean verifyCheckResponse(JsonObject jsonObject) {
        Gson gson = new Gson();
        CheckResponse checkResponse = gson.fromJson(jsonObject.get("response").getAsJsonObject(), CheckResponse.class);
        ArrayList<PendingTransfer> pendingTransfers = checkResponse.getTransfers();
        for (PendingTransfer pendingTransfer : pendingTransfers) {
            PublicKey sender = KeyConversion.stringToKey(pendingTransfer.sender());
            PublicKey receiver = KeyConversion.stringToKey(pendingTransfer.receiver());
            SendAmountRequest sendAmountRequest = new SendAmountRequest(sender, receiver, pendingTransfer.amount());
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("requestType", "sendAmount");
            requestJson.add("request", JsonParser.parseString(gson.toJson(sendAmountRequest)));
            JsonObject requestJsonWts = makeWriteMsg(requestJson, pendingTransfer.getWts(), pendingTransfer.getRid());
            try {
                if (!StringSignature.verify(requestJsonWts.toString(), pendingTransfer.getSenderSignature(), sender)) {
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
     * Verifies if the readIds match the one sent
     * @param jsonObjects An Array of JsonObject containing messages that need to be verified
     * @param readId The readId sent
     * @return True if the number of matching readId is equal or larger to the quorum size. Otherwise returns false.
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
     * Broadcast a JsonObject containing a read operation
     * Since it is an expensive operation, a proof of work is required to be complete before starting the operation
     * to avoid DDOS. Since we are using a Write-Majority algorithm, after the read, we broadcast the value with the
     * highest timestamp to all replicas, which is what makes the operation expensive.
     * @param object A JsonObject that contains a read operation that will be broadcast to all replicas
     * @return A value received as a response to the broadcast read operation
     */

    public String read(JsonObject object) {
        proofOfWork(object, difficulty);
        var readId = generateReadId();
        var readMsg = makeReadMsg(object, readId);
        reading = true;
        broadcastChannel.broadcastMsg(readMsg);
        var msgs = broadcastChannel.receiveMsgs();
        JsonObject highestValue;
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            highestValue = highestValue(msgs);
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
            JsonObject highestValueResponse = highestValue.get("response").getAsJsonObject();
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
     * Creates a JsonObject containing WriteBackCheckRequest from a JsonObject containing an CheckResponse
     * which contains the transfer that will be needed to be written to update replicas.
     * @param jsonObject A JsonObject containing an CheckResponse
     * @return A JsonObject containing a WriteBackCheckRequest.
     */

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
                sendAmountRequest.addProperty("signature", pendingTransfer.getSenderSignature());
                necessaryMessages.add(sendAmountRequest);
            }
            writeBackCheckRequest = new WriteBackCheckRequest(necessaryMessages, publicKey);
        }
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("requestType", "writeBackCheck");
        requestJson.add("request", JsonParser.parseString(gson.toJson(writeBackCheckRequest)));
        return requestJson;
    }

    /**
     * Creates a JsonObject containing WriteBackAuditRequest from a JsonObject containing an AuditResponse
     * which contains the transfer that will be needed to be written to update replicas.
     * @param jsonObject A JsonObject containing an AuditResponse
     * @return A JsonObject containing a WriteBackAuditRequest.
     */

    public JsonObject writeBackAudit(JsonObject jsonObject){
        ArrayList<JsonObject> necessaryMessages = new ArrayList<>();
        JsonObject response = jsonObject.get("response").getAsJsonObject();
        String type = response.get("type").getAsString();
        Gson gson = new Gson();
        WriteBackAuditRequest writeBackAuditRequest = new WriteBackAuditRequest(necessaryMessages, publicKey);
        if (type.equals("Audit")) {
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
     * Broadcasts a request for the timestamp
     * @param object A JsonObject that contains a request for a timestamp
     * @return A JsonObject containing the new timestamp
     */

    public JsonObject readTS(JsonObject object) {
        var readId = generateReadId();
        var readMsg = makeReadMsg(object, readId);
        reading = true;
        broadcastChannel.broadcastMsg(readMsg);

        var msgs = broadcastChannel.receiveMsgs();
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            return highestValue(msgs);
        } else {
            throw new RuntimeException("Too many crashes");
        }
    }

    /**
     * Sets the timestamp of the replica
     * @param timestamp The value we want the timestamp to take.
     */

    public void setTimestamp(AtomicLong timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Constructs a JsonObject of SendAmountRequest from an PendingTransfer,
     * the public Key of its sender, the public Key of its receiver and the amount.
     * @param pendingTransfer The pending transfer
     * @param sender The publicKey of the sender
     * @param receiver The publicKey of the receiver
     * @param amount The amount of the transfer
     * @return A JsonObject containing a SendAmountRequest, a wts and a rid.
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
     *  Constructs a JsonObject of ReceiveAmountRequest from an AcceptedTransfer,
     * the public Key of its sender and the public Key of its receiver.
     * @param acceptedTransfer The accepted transfer
     * @param sender The publicKey of the sender
     * @param receiver The publicKey of the receiver
     * @return A JsonObject containing a ReceiveAmountRequest, a wts and a rid.
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

    /**
     * Getter for the public key
     * @return The public key
     */

    public String getPublicKey() {
        return publicKey;
    }

    public ArrayList<String> getQuorumResponses(ArrayList<String> responses){
        ArrayList<String> validResponses = new ArrayList<>();
        HashSet<String> responsesHashSet = new HashSet<>(responses);
        for(String response: responsesHashSet){
            if(hasQuorumSize(Collections.frequency(responses, response))){
                validResponses.add(response);
            }
        }
        return validResponses;
    }
}