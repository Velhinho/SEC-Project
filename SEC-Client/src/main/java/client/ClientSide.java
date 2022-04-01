package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import communication.channel.Channel;
import communication.messages.*;

import java.security.PublicKey;

public class ClientSide {
    private final Channel channel;

    public Channel getChannel() {
        return channel;
    }

    public PublicKey ourPublicKey;

    public ClientSide(Channel channel, PublicKey ourPublicKey) {
        this.channel = channel;
        this.ourPublicKey = ourPublicKey;
    }

    private JsonObject makeRequest(String requestType, Object request) {
        var gson = new Gson();

        var requestJson = new JsonObject();
        requestJson.addProperty("requestType", requestType);
        requestJson.add("request", JsonParser.parseString(gson.toJson(request)));
        return requestJson;
    }

    public String openAccount(PublicKey publicKey) throws Exception {
        var request = new OpenAccountRequest(publicKey);
        var requestJson = makeRequest("openAccount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }

    public String sendAmountRequest(PublicKey sender, PublicKey receiver, int ammount) throws Exception {
        var request = new SendAmountRequest(sender, receiver, ammount);
        var requestJson = makeRequest("sendAmount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }

    public String checkAccount(PublicKey publicKey) throws Exception {
        var request = new CheckAccountRequest(ourPublicKey, publicKey);
        var requestJson = makeRequest("checkAccount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }

    public String receiveAmountRequest(PublicKey sender, PublicKey receiver) throws Exception {
        var request = new ReceiveAmountRequest(sender, receiver);
        var requestJson = makeRequest("receiveAmount", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }

    public String audit(PublicKey publicKey) throws Exception {
        var request = new AuditRequest(ourPublicKey,publicKey);
        var requestJson = makeRequest("audit", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }
}
