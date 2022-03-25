package communication.channel;

import com.google.gson.JsonObject;

public interface Channel {
    void sendMessage(JsonObject jsonObject) throws ChannelException;
    JsonObject receiveMessage() throws ChannelException;
}
