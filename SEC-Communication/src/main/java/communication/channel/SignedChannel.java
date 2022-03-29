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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Objects;

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
            var nonce = askNonce();
            var jsonWithNonce = appendNonce(jsonObject, nonce);
            var signature = StringSignature.sign(jsonWithNonce.toString(), getPrivateKey());
            var message = appendSignature(jsonWithNonce, signature);

            var writer = new PrintWriter(getSocket().getOutputStream());
            writer.println(message);
            writer.flush();
        } catch (Exception exception) {
            throw new ChannelException(exception.getMessage());
        }
    }

    /*
     *
     * {"func_call": "check_account", ...}
     *
     * {"jsonObject": {"func_call": "check_account", ...}, "nonce": 123123}
     *
     * {"jsonWithNonce": {"jsonObject": {"func_call": "check_account", ...}, "nonce": 123123}, "signature": asdadsd}
     * */

    private JsonObject unpackSignature(JsonObject message) throws ChannelException, CryptoException {
        var jsonWithNonce = message.getAsJsonObject("jsonWithNonce");
        var signature = message.get("signature").getAsString();

        if (StringSignature.verify(jsonWithNonce.toString(), signature, getPublicKey())) {
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
            var givenNonce = giveNonce();
            var reader = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
            var message = JsonParser.parseString(reader.readLine()).getAsJsonObject();
            System.out.println("message: " + message + "\n");
            var jsonWithNonce = unpackSignature(message);
            return unpackNonce(jsonWithNonce, givenNonce);
        } catch (IOException | CryptoException exception) {
            throw new ChannelException(exception.getMessage());
        }
    }
}
