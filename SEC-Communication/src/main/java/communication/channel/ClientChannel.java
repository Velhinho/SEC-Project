package communication.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.CryptoException;
import communication.crypto.KeyConversion;
import communication.crypto.StringSignature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;

public class ClientChannel implements Channel {
    private final Socket socket;
    private final PrivateKey privateKey;

    public ClientChannel(Socket socket, PrivateKey privateKey) {
        this.socket = socket;
        this.privateKey = privateKey;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    private JsonObject appendSignature(JsonObject jsonObject, String signature) {
        // newJson = {"signature": ..., "jsonObject": {"key": ..., ...}}

        var newJson = new JsonObject();
        newJson.addProperty("signature", signature);
        newJson.add("jsonObject", jsonObject);
        return newJson;
    }

    @Override
    public void sendMessage(JsonObject jsonObject) throws ChannelException {
        try {
            var signature = StringSignature.sign(jsonObject.toString(), getPrivateKey());
            var message = appendSignature(jsonObject, signature);

            var writer = new PrintWriter(getSocket().getOutputStream());
            writer.println(message);
            writer.flush();
        } catch (Exception exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    private JsonObject unpackSignature(JsonObject message) throws ChannelException, CryptoException {
        // message = {"signature": ..., "jsonObject": {"request": {"key": ..., ...}}}

        var signature = message.get("signature").getAsString();
        var jsonObject = message.get("jsonObject").getAsJsonObject();
        var key = message.get("jsonObject")
                .getAsJsonObject()
                .get("key")
                .getAsString();
        var publicKey = KeyConversion.stringToKey(key);
        if (StringSignature.verify(jsonObject.toString(), signature, publicKey)) {
            return jsonObject;
        } else {
            throw new ChannelException("Wrong signature");
        }
    }

    @Override
    public JsonObject receiveMessage() throws ChannelException {
        try {
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var message = JsonParser.parseString(reader.readLine()).getAsJsonObject();
            System.out.println("message: " + message + "\n");
            return unpackSignature(message);
        } catch (IOException | CryptoException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }
}
