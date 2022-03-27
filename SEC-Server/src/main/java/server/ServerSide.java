package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.channel.Channel;
import communication.channel.ChannelException;
import communication.messages.*;
//import server.data.ServerData;
import server.data.ServerDataDB;

import java.util.List;
import java.util.Objects;

public class ServerSide {
    private final Channel channel;

    private static ServerDataDB serverData = new ServerDataDB();

    public Channel getChannel() {
        return channel;
    }

    public ServerSide(Channel channel) {
        this.channel = channel;
    }



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

        if (Objects.equals(requestType, "checkAccount")) {
            var request = gson.fromJson(requestJson.get("request"), CheckAccountRequest.class);
            System.out.println("checkAccount: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "audit")) {
            var request = gson.fromJson(requestJson.get("request"), AuditRequest.class);
            System.out.println("audit: " + request);

            var response = List.of();
            var responseJson = makeResponse(response);
            getChannel().sendMessage(responseJson);

        } else if (Objects.equals(requestType, "openAccount")) {
            var request = gson.fromJson(requestJson.get("request"), OpenAccountRequest.class);

            System.out.println("openAccount: " + request);
            System.out.println("\n");
            openAccount(request.getPublicKey());

        } else if (Objects.equals(requestType, "sendAmount")) {
            var request = gson.fromJson(requestJson.get("request"), SendAmountRequest.class);
            System.out.println("SendAmount: " + request);
            //sendAmount(request.getSender(), request.getReceiver(), request.getAmount());

        } else if (Objects.equals(requestType, "receiveAmount")) {
            var request = gson.fromJson(requestJson.get("request"), ReceiveAmountRequest.class);
            System.out.println("receiveAmount: " + request);
            //receiveAmount(request.getSender(), request.getReceiver());
        }
        else {
            throw new RuntimeException("invalid json message type");
        }
    }



    private void openAccount(String publicKey){
        serverData.openAccount(publicKey);
    }

    /*

    private void sendAmount(String sender, String receiver, int amount) {
        serverData.sendAmount(sender, receiver, amount);
    }

    private void receiveAmount(String sender, String receiver){
        serverData.receiveAmount(sender, receiver);
    }

    private String audit(String publicKey){
        return serverData.auditAccount(publicKey).toString();
    }

    private String checkAccount(String publicKey){
        return "Account Balance: " + serverData.checkAccountBalance(publicKey) + " \n" + serverData.checkAccountTransfers(publicKey).toString();
    }

     */

}
