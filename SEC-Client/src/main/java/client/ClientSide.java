package client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import communication.channel.Channel;
import communication.channel.SignedChannel;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;
import communication.messages.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class ClientSide {
    private final SignedChannel channel;

    private final PublicKey ourPublicKey;

    public SignedChannel getChannel() {
        return channel;
    }

    public ClientSide(SignedChannel channel, PublicKey ourPublicKey) {
        this.channel = channel;
        this.ourPublicKey = ourPublicKey;
    }

    public boolean sendPublicKey() throws IOException, CryptoException {
        var writer = new PrintWriter(channel.getSocket().getOutputStream());
        writer.println(KeyConversion.keyToString(ourPublicKey));
        writer.flush();
        var reader = new BufferedReader(new InputStreamReader(channel.getSocket().getInputStream()));
        var response = reader.readLine();
        return StringSignature.verify("Key Passed With Success", response, channel.getPublicKey());
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
        var request = new CheckAccountRequest(publicKey);
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
        var request = new AuditRequest(publicKey);
        var requestJson = makeRequest("audit", request);
        getChannel().sendMessage(requestJson);

        var gson = new Gson();
        var responseJson = getChannel().receiveMessage().get("response");
        var response =  gson.fromJson(responseJson, new TypeToken<String>(){}.getType());
        System.out.println(response);
        return (String) response;
    }
}
