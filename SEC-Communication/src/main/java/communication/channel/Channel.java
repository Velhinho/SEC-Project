package communication.channel;

import com.google.gson.JsonObject;

import java.io.IOException;

public interface Channel {
    void sendMessage(JsonObject jsonObject) throws ChannelException;
    JsonObject receiveMessage() throws ChannelException;
    void closeSocket() throws IOException;
}
