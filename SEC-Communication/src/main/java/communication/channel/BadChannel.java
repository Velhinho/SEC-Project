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

public class BadChannel implements Channel {
    private final Socket socket;
    private final PrivateKey privateKey;
    private long nonce;

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public Socket getSocket() {
        return socket;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public BadChannel(Socket socket, PrivateKey privateKey){
        this.socket = socket;
        this.privateKey = privateKey;
    }

    private long askNonce() throws IOException {
        var writer = new PrintWriter(getSocket().getOutputStream());
        System.out.println("Asked for nonce");
        writer.println("nonce pls");
        writer.flush();
        var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        var nonce = reader.readLine();
        System.out.println("Read nonce");
        return Long.parseLong(nonce);
    }

    private JsonObject appendNonce(JsonObject jsonObject, long nonce) {
        var newJson = new JsonObject();
        newJson.addProperty("nonce", nonce);
        newJson.add("jsonObject", jsonObject);
        return newJson;
    }

    private JsonObject appendSignature(JsonObject jsonObject, String signature) {
        var newJson = new JsonObject();
        newJson.addProperty("signature", signature + "asd");
        newJson.add("jsonWithNonce", jsonObject);
        return newJson;
    }

    @Override
    public void sendMessage(JsonObject jsonObject) throws ChannelException {
        try {
            setNonce(askNonce());
            var jsonWithNonce = appendNonce(jsonObject, getNonce());
            var signature = StringSignature.sign(jsonWithNonce.toString(), getPrivateKey());
            var message = appendSignature(jsonWithNonce, signature);

            var writer = new PrintWriter(getSocket().getOutputStream());
            writer.println(message);
            writer.flush();
        } catch (Exception exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    private JsonObject unpackSignature(JsonObject message) throws ChannelException, CryptoException {
        var jsonWithNonce = message.getAsJsonObject("jsonWithNonce");
        var signature = message.get("signature").getAsString();
        var key = message.get("jsonWithNonce")
                .getAsJsonObject()
                .get("jsonObject")
                .getAsJsonObject()
                .get("key")
                .getAsString();
        var publicKey = KeyConversion.stringToKey(key);
        if (StringSignature.verify(jsonWithNonce.toString(), signature, publicKey)) {
            return jsonWithNonce;
        } else {
            throw new ChannelException("Wrong signature");
        }
    }

    private JsonObject unpackNonce(JsonObject jsonWithNonce, long givenNonce) throws ChannelException {
        var jsonObject = jsonWithNonce.getAsJsonObject("jsonObject");
        var receivedNonce = jsonWithNonce.get("nonce").getAsLong();

        if(givenNonce == receivedNonce) {
            return jsonObject;
        } else {
            throw new ChannelException("Wrong nonce");
        }
    }

    @Override
    public JsonObject receiveMessage() throws ChannelException {
        try {
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var message = JsonParser.parseString(reader.readLine()).getAsJsonObject();
            System.out.println("message: " + message + "\n");
            var jsonWithNonce = unpackSignature(message);
            return unpackNonce(jsonWithNonce, getNonce());
        } catch (IOException | CryptoException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    public void closeSocket() throws IOException{
        socket.close();
    }
}
