package communication.channel;

import com.google.gson.JsonObject;

import java.util.ArrayList;

public class BroadcastChannel {
    private final ArrayList<Channel> channels;

    public BroadcastChannel(ArrayList<Channel> channels) {
        this.channels = channels;
    }

    public ArrayList<Channel> getChannels() {
        return channels;
    }

    public void broadcastMsg(JsonObject jsonObject) {
        for (var c : getChannels()) {
            try {
                c.sendMessage(jsonObject);
            } catch (ChannelException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public ArrayList<JsonObject> receiveMsgs() {
        var msgs = new ArrayList<JsonObject>();

        for (var c : getChannels()) {
            try {
                var msg = c.receiveMessage();
                msgs.add(msg);
            } catch (ChannelException e) {
                e.printStackTrace(System.err);
            }
        }
        return msgs;
    }
}
