package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.Channel;
import communication.channel.ChannelException;
import communication.messages.AuditRequest;
import communication.messages.CheckAccountRequest;

import java.util.List;
import java.util.Objects;

public class ServerSide {
    private final Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public ServerSide(Channel channel) {
        this.channel = channel;
    }

    /*
    *
    * {"func_call": "check_account", ...}
    *
    *
    * {"request": {"func_call": "check_account", ...}, "signature": asdadsd}
    * */

    private JsonObject makeResponse(Object response) {
        var gson = new Gson();
        var responseJson = new JsonObject();
        responseJson.add("response", JsonParser.parseString(gson.toJson(response)));
        return responseJson;
    }

    public void processRequest() throws RuntimeException, ChannelException {
        var requestJson = getChannel().receiveMessage();
        var requestType = requestJson.get("requestType").getAsString();
        var gson = new Gson();
        System.out.println("Json: " + requestJson);

        if(Objects.equals(requestType, "checkAccount")) {
            var request = gson.fromJson(requestJson.get("request"), CheckAccountRequest.class);
            System.out.println("checkAccount: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if(Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(requestJson.get("request"), AuditRequest.class);
            System.out.println("audit: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else {
            throw new RuntimeException("invalid json message type");
        }
    }
}
