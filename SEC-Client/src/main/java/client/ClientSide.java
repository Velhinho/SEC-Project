package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import communication.channel.Channel;
import communication.messages.AuditRequest;
import communication.messages.CheckAccountRequest;
import communication.messages.Transfer;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class ClientSide {
    private final Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public ClientSide(Channel channel) {
        this.channel = channel;
    }

    private JsonObject makeRequest(String requestType, Object request) {
        var gson = new Gson();

        var requestJson = new JsonObject();
        requestJson.addProperty("requestType", requestType);
        requestJson.add("request", JsonParser.parseString(gson.toJson(request)));
        return requestJson;
    }

    List<Long> checkAccount(PublicKey publicKey) throws Exception {
        var request = new CheckAccountRequest(123);
        var requestJson = makeRequest("checkAccount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        return gson.fromJson(responseJson, new TypeToken<ArrayList<Long>>(){}.getType());
    }

    List<Transfer> audit(PublicKey publicKey) throws Exception {
        var request = new AuditRequest(publicKey);
        var requestJson = makeRequest("audit", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        return gson.fromJson(responseJson, new TypeToken<ArrayList<Transfer>>(){}.getType());
    }
}
