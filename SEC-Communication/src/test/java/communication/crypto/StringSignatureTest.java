package communication.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import communication.channel.ChannelException;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StringSignatureTest {
    @Test
    void sign_verify_test() {
        assertDoesNotThrow(() -> {
            var keyPair = RSAKeyGenerator.generateKeyPair();
            var jsonObject = new JsonObject();
            jsonObject.addProperty("test", 123);
            var message = new JsonObject();
            var signature = StringSignature.sign(jsonObject.toString(), keyPair.getPrivate());
            message.addProperty("signature", signature);
            message.add("jsonObject", jsonObject);
            System.out.println(message);

            assertTrue(StringSignature.verify(jsonObject.toString(), signature, keyPair.getPublic()));
        });
    }
}