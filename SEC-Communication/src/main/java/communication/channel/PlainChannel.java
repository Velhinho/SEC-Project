package communication.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlainChannel implements Channel {
    private final Socket socket;

    public PlainChannel(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    /*
    * Transfers(from, to, amount, order)
    * */

    @Override
    public void sendMessage(JsonObject jsonObject) throws ChannelException {
        try {
            if (jsonObject != null) {
                var writer = new PrintWriter(getSocket().getOutputStream());
                writer.println(jsonObject);
                writer.flush();
            }
        } catch (IOException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    @Override
    public JsonObject receiveMessage() throws ChannelException {
        try {
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var line = reader.readLine();
            return JsonParser.parseString(line).getAsJsonObject();
        } catch (IOException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }
}
