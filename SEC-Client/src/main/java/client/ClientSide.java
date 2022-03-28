package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import communication.channel.Channel;
import communication.messages.*;

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

    public void openAccount(PublicKey publicKey) throws Exception {
        var request = new OpenAccountRequest(publicKey);
        var requestJson = makeRequest("openAccount", request);
        getChannel().sendMessage(requestJson);
    }

    public void sendAmountRequest(PublicKey sender, PublicKey receiver, int ammount) throws Exception {
        var request = new SendAmountRequest(sender, receiver, ammount);
        var requestJson = makeRequest("sendAmount", request);
        getChannel().sendMessage(requestJson);
    }

    public void checkAccount(PublicKey publicKey) throws Exception {
        var request = new CheckAccountRequest(publicKey);
        var requestJson = makeRequest("checkAccount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<ArrayList<Integer>>(){}.getType());
        System.out.println(response);
    }

    public void receiveAmountRequest(PublicKey sender, PublicKey receiver) throws Exception {
        var request = new ReceiveAmountRequest(sender, receiver);
        var requestJson = makeRequest("receiveAmount", request);
        getChannel().sendMessage(requestJson);
    }

    public void audit(PublicKey publicKey) throws Exception {
        var request = new AuditRequest(publicKey);
        var requestJson = makeRequest("audit", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<ArrayList<Integer>>(){}.getType());
        System.out.println(response);
    }
}
