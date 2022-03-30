import client.ClientSide;
import communication.channel.Channel;
import communication.channel.PlainChannel;
import communication.crypto.KeyConversion;
import org.junit.jupiter.api.*;

import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientSideTest {

    static Socket socket;
    static Channel channel;
    static ClientSide clientSide;
    static KeyPair keyPair;
    static PublicKey publicKey;
    static Socket socket2;
    static Channel channel2;
    static ClientSide clientSide2;
    static KeyPair keyPair2;
    static PublicKey publicKey2;

    @BeforeAll
    static void setupTests() throws Exception{
        socket = new Socket("localhost", 8080);
        socket2 = new Socket("localhost", 8080);
        channel = new PlainChannel(socket);
        channel2 = new PlainChannel(socket2);
        clientSide = new ClientSide(channel);
        clientSide2 = new ClientSide(channel2);
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator keyPairGen2 = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        keyPairGen2.initialize(2048);
        keyPair = keyPairGen.generateKeyPair();
        keyPair2 = keyPairGen2.generateKeyPair();
        publicKey = keyPair.getPublic();
        publicKey2 = keyPair2.getPublic();
    }

    @Test
    @Order(1)
    void openAnAccount() throws Exception {
        assertEquals("Account Opened With Success!",clientSide.openAccount(publicKey));
    }

    @Test
    @Order(2)
    void openAnAlreadyOpenAccount() throws Exception {
        String publicKeyString = KeyConversion.keyToString(publicKey);
        assertEquals("Account with Public Key = " + publicKeyString + "already exists" ,clientSide.openAccount(publicKey));
    }

    @Test
    @Order(3)
    void openAnotherAccount() throws Exception {
        assertEquals("Account Opened With Success!",clientSide.openAccount(publicKey2));
    }

    @Test
    @Order(4)
    void SendMoreThanBalanceAmount() throws Exception {
    }




}