package client;

import com.google.gson.JsonObject;
import communication.channel.BroadcastChannel;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class Register {
    private final AtomicLong timestamp = new AtomicLong(0);
    private final BroadcastChannel broadcastChannel;
    private final int quorumSize;

    public Register(BroadcastChannel broadcastChannel, int quorumSize) {
        this.broadcastChannel = broadcastChannel;
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

    public void write(JsonObject jsonObject) {
        var timestamp = getTimestamp().incrementAndGet();
        var writeMsg = makeWriteMsg(jsonObject, timestamp);
        getBroadcastChannel().broadcastMsg(writeMsg);

        var acks = getBroadcastChannel().receiveMsgs();
        if (acks.size() <= getQuorumSize()) {
            throw new RuntimeException("Not enough ACKs");
        }
    }

    private static long generateReadId() {
        try {
            var rng = SecureRandom.getInstance("SHA1PRNG");
            return rng.nextLong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static JsonObject makeReadMsg(long readId) {
        // {"readId": 123}
        var jsonWithReadId = new JsonObject();
        jsonWithReadId.addProperty("readId", readId);
        return jsonWithReadId;
    }

    private boolean verifySignatures(ArrayList<JsonObject> jsonObjects) {
        //FIXME not checking signatures
        return true;
    }

    private JsonObject highestValue(ArrayList<JsonObject> jsonObjects) {
        //FIXME not checking highest timestamp
        //FIXME not checking highest number of transfers
        return jsonObjects.get(0);
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

    public JsonObject read() {
        var readId = generateReadId();
        var readMsg = makeReadMsg(readId);
        broadcastChannel.broadcastMsg(readMsg);

        var msgs = broadcastChannel.receiveMsgs();
        if (checkReadIds(msgs, readId) && verifySignatures(msgs) && hasQuorumSize(msgs.size())) {
            return highestValue(msgs);
        } else {
            throw new RuntimeException("Too many crashes");
        }
    }
}
