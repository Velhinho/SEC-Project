package communication.channel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import communication.crypto.CryptoException;
import communication.crypto.StringSignature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;

public class SignedChannel implements Channel {
    private final Socket socket;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public Socket getSocket() {
        return socket;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public SignedChannel(Socket socket, PublicKey publicKey, PrivateKey privateKey) {
        this.socket = socket;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void sendMessage(JsonObject jsonObject) throws ChannelException {
        try {
            var message = new JsonObject();
            var writer = new PrintWriter(getSocket().getOutputStream());
            var signature = StringSignature.sign(jsonObject.toString(), getPrivateKey());
            message.addProperty("signature", signature);
            message.add("jsonObject", jsonObject);
            writer.println(message);
            writer.flush();
        } catch (Exception exception) {
            throw new ChannelException(exception);
        }
    }

    /*
     *
     * {"func_call": "check_account", ...}
     *
     *
     * {"jsonObject": {"func_call": "check_account", ...}, "signature": asdadsd}
     * */


    @Override
    public JsonObject receiveMessage() throws ChannelException {
        try {
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var message = JsonParser.parseString(reader.readLine()).getAsJsonObject();
            var jsonObject = message.getAsJsonObject("jsonObject");
            var signature = message.get("signature").getAsString();

            if (StringSignature.verify(jsonObject.toString(), signature, getPublicKey())) {
                return jsonObject;
            } else {
                throw new RuntimeException("Wrong signature");
            }
        } catch (IOException | CryptoException exception) {
            throw new ChannelException(exception);
        }
    }
}
