package communication.channel;

import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;

public class BroadcastChannel {
    private final ArrayList<ClientChannel> channels;

    public BroadcastChannel(ArrayList<ClientChannel> channels) {
        this.channels = channels;
    }

    public ArrayList<ClientChannel> getChannels() {
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

    public void broadcastDirectMsg(JsonObject jsonObject) {
        for (var c : getChannels()) {
            try {
                c.sendDirectMessage(jsonObject);
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

    public void closeSocket() throws IOException {
        for(int i = 0; i < channels.size(); i++){
            this.channels.get(i).closeSocket();
        }
    }

}
