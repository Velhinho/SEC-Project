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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.Objects;

public class ServerChannel implements Channel {
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

    public ServerChannel(Socket socket, PrivateKey privateKey){
        this.socket = socket;
        this.privateKey = privateKey;
    }

    private long giveNonce() throws ChannelException {
        try {
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var line = reader.readLine();
            System.out.println("Waiting for nonce request");
            if (Objects.equals(line, "nonce pls")) {
                var rng = SecureRandom.getInstance("SHA1PRNG");
                var writer = new PrintWriter(getSocket().getOutputStream());
                var nonce = rng.nextLong();
                writer.println(nonce);
                writer.flush();
                System.out.println("Gave nonce");
                return nonce;
            } else {
                throw new ChannelException("Didn't ask for nonce");
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    private JsonObject appendNonce(JsonObject jsonObject, long nonce) {
        var newJson = new JsonObject();
        newJson.addProperty("nonce", nonce);
        newJson.add("jsonObject", jsonObject);
        return newJson;
    }

    private JsonObject appendSignature(JsonObject jsonObject, String signature) {
        var newJson = new JsonObject();
        newJson.addProperty("signature", signature);
        newJson.add("jsonWithNonce", jsonObject);
        return newJson;
    }

    @Override
    public void sendMessage(JsonObject jsonObject) throws ChannelException {
        try {
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
            .get("request")
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
            setNonce(giveNonce());
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
